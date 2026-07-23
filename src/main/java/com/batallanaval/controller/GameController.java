package com.batallanaval.controller;

import com.batallanaval.ai.HuntTargetShootingStrategy;
import com.batallanaval.exception.InvalidPlacementException;
import com.batallanaval.exception.InvalidShotException;
import com.batallanaval.exception.PersistenceException;
import com.batallanaval.model.Board;
import com.batallanaval.model.Fleet;
import com.batallanaval.model.HumanPlayer;
import com.batallanaval.model.MachinePlayer;
import com.batallanaval.model.RandomFleetPlacer;
import com.batallanaval.model.Ship;
import com.batallanaval.model.ShipFactory;
import com.batallanaval.model.Shot;
import com.batallanaval.persistence.GameState;
import com.batallanaval.persistence.GamePersistenceManager;
import com.batallanaval.persistence.GameSummary;
import com.batallanaval.util.Coordinate;
import com.batallanaval.util.GameClock;
import com.batallanaval.util.GamePhase;
import com.batallanaval.util.Orientation;
import com.batallanaval.util.ShotResult;
import com.batallanaval.util.GameSession;
import com.batallanaval.util.ShipType;
import com.batallanaval.util.GameTurn;
import com.batallanaval.util.SoundManager;
import com.batallanaval.view.Board3DView;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestrates the whole game: placement phase (HU-1), human/machine
 * shooting turns (HU-2/HU-4), enemy board verification (HU-3), and victory
 * detection. This is the "Controller" of the MVC architecture: it knows
 * the Model (Board, Player) and the View (Board3DView), but the View and
 * the Model do not know each other directly (except through the Observer
 * BoardListener, which already decouples that relationship).
 */
public class GameController {

    private static final Logger LOG = Logger.getLogger(GameController.class.getName());

    private final Board humanPositionBoard = new Board();
    private final Board machineMainBoard = new Board();

    private final Board3DView positionView;
    private final Board3DView mainView;
    private final Label statusLabel;
    private final GameClock gameClock;

    private final GamePersistenceManager persistenceManager = new GamePersistenceManager();

    private HumanPlayer human;
    private MachinePlayer machine;

    private GamePhase phase = GamePhase.PLACEMENT;
    private GameTurn turn = GameTurn.HUMAN;

    // Deque (no List): la cola de colocacion se consume estrictamente FIFO
    // (el barco de encabezado sale con pollFirst() al colocarlo), asi que un
    // Deque expresa esa intencion mejor que una lista de proposito general.
    private final Deque<ShipType> placementQueue = new ArrayDeque<>();
    private Orientation currentOrientation = Orientation.RIGHT;
    private boolean verificationMode = false;

    private Consumer<Boolean> onVerificationAvailabilityChanged = available -> { };
    private Consumer<Boolean> onPlacementAvailabilityChanged = available -> { };

    public GameController(Pane positionContainer, Pane mainContainer, Label statusLabel, Label timerLabel) {
        this.statusLabel = statusLabel;
        this.gameClock = new GameClock(timerLabel);

        positionView = new Board3DView(humanPositionBoard, true);
        mainView = new Board3DView(machineMainBoard, false);

        positionContainer.getChildren().add(positionView.getNode());
        mainContainer.getChildren().add(mainView.getNode());

        humanPositionBoard.addListener(positionView);
        machineMainBoard.addListener(mainView);

        positionView.setOnCellClick(this::onPositionBoardClick);
        positionView.setOnCellHoverEnter(this::onPositionBoardHover);
        positionView.setOnCellHoverExit(coordinate -> positionView.clearPreview());
        mainView.setOnCellClick(this::onMainBoardClick);

        if (persistenceManager.hasSavedGame()) {
            loadSavedGame();
        } else {
            preparePlacementQueue();
            updatePlacementStatus();
            setUpMachine();
        }
    }

    /**
     * Places the machine's fleet and creates the MachinePlayer as soon as
     * the game is entered, not only once the human finishes placing theirs:
     * this way the enemy board already has ships if the compass is used to
     * verify it before shooting starts, instead of looking empty.
     */
    private void setUpMachine() {
        RandomFleetPlacer.placeRandomFleet(machineMainBoard);
        machine = new MachinePlayer("Maquina", machineMainBoard, new HuntTargetShootingStrategy());
        mainView.redrawAll();
    }

    /**
     * Rebuilds the game from the save files (persistence user story):
     * copies the deserialized boards' state into the already-constructed
     * boards (so the view's listeners aren't invalidated) and resumes
     * phase/turn/placement queue exactly as they were left.
     */
    private void loadSavedGame() {
        try {
            GameState state = persistenceManager.loadState();
            GameSummary summary = persistenceManager.loadSummary();

            humanPositionBoard.restoreState(state.getHumanPositionBoard());
            machineMainBoard.restoreState(state.getMachineMainBoard());

            phase = state.getPhase();
            turn = state.getTurn();
            placementQueue.clear();
            placementQueue.addAll(state.getPlacementQueue());
            currentOrientation = state.getCurrentOrientation();
            verificationMode = state.isVerificationMode();

            positionView.redrawAll();
            mainView.setVerificationMode(verificationMode);

            if (phase == GamePhase.PLACEMENT) {
                updatePlacementStatus();
                // red de seguridad para partidas guardadas antes de que la
                // flota de la maquina se colocara desde el arranque.
                if (machineMainBoard.getFleet() == null) {
                    setUpMachine();
                }
                return;
            }

            human = new HumanPlayer(summary.getHumanNickname(), humanPositionBoard);
            machine = new MachinePlayer(summary.getMachineNickname(), machineMainBoard,
                    new HuntTargetShootingStrategy());

            if (phase == GamePhase.FINISHED) {
                statusLabel.setText("Partida cargada: la partida ya habia finalizado.");
            } else {
                statusLabel.setText("Partida cargada. Turno: " + turn);
                gameClock.start();
                if (turn == GameTurn.MACHINE) {
                    pauseThen(this::machineTurn);
                }
            }
        } catch (PersistenceException e) {
            LOG.log(Level.WARNING, "No se pudo cargar la partida guardada, se inicia una nueva.", e);
            statusLabel.setText("No se pudo cargar la partida guardada, se inicia una nueva.");
            preparePlacementQueue();
            updatePlacementStatus();
        }
    }

    /**
     * Autosave (persistence user story): saves the boards' full state
     * (serializable file) and a readable summary with nickname and sunk
     * ships (plain text file) every time the game's state changes. An I/O
     * failure must not interrupt the game, it is only logged.
     */
    private void autoSave() {
        GameState state = new GameState(humanPositionBoard, machineMainBoard, phase, turn,
                new ArrayList<>(placementQueue), currentOrientation, verificationMode);
        GameSummary summary = new GameSummary(
                human != null ? human.getNickname() : "Jugador",
                machine != null ? machine.getNickname() : "Maquina",
                humanPositionBoard.getFleet() != null
                        ? (int) humanPositionBoard.getFleet().countSunkShips() : 0,
                humanPositionBoard.getFleet() != null ? humanPositionBoard.getFleet().getTotalShips() : 0,
                machineMainBoard.getFleet() != null
                        ? (int) machineMainBoard.getFleet().countSunkShips() : 0,
                machineMainBoard.getFleet() != null ? machineMainBoard.getFleet().getTotalShips() : 0);
        try {
            persistenceManager.saveGame(state, summary);
        } catch (PersistenceException e) {
            LOG.log(Level.WARNING, "No se pudo guardar la partida automaticamente.", e);
        }
    }

    private void preparePlacementQueue() {
        for (ShipType type : ShipType.values()) {
            for (int i = 0; i < type.getCountInFleet(); i++) {
                placementQueue.add(type);
            }
        }
    }

    /**
     * Invoked by the "Rotate ship" button (or the R key): rotates the
     * orientation 90 degrees clockwise each time (right, down, left, up,
     * and back to right), so the ship can be pointed at any of the 4 sides
     * before placing it.
     */
    public void toggleOrientation() {
        Orientation[] orientations = Orientation.values();
        int nextIndex = (currentOrientation.ordinal() + 1) % orientations.length;
        currentOrientation = orientations[nextIndex];
        if (phase == GamePhase.PLACEMENT) {
            updatePlacementStatus();
            positionView.clearPreview();
        }
    }

    /**
     * Invoked by the compass button (verification mode, HU-3). Returns the
     * new state so the view can reflect it (e.g. highlighting the button
     * while the mode is active).
     *
     * NOTE: the assignment (HU-3) asks for this option to be "for
     * verification purposes only and not [available] during normal
     * gameplay." Per the user's explicit request, that restriction was
     * left out (canVerify() no longer checks the phase): the button can be
     * turned on/off at any time, even though that departs from the letter
     * of that user story.
     */
    public boolean toggleVerification() {
        if (!canVerify()) {
            return verificationMode;
        }
        verificationMode = !verificationMode;
        mainView.setVerificationMode(verificationMode);
        autoSave();
        return verificationMode;
    }

    /** Current state of verification mode, so the view can reflect it when loading a saved game. */
    public boolean isVerificationMode() {
        return verificationMode;
    }

    /**
     * Always available (see the note on {@link #toggleVerification()}).
     * The method is kept (instead of removed) so the view-notification
     * mechanism (setOnVerificationAvailabilityChanged) isn't dismantled, in
     * case the restriction is restored later on.
     */
    private boolean canVerify() {
        return true;
    }

    /**
     * Lets the view (MainController) know when the verification button
     * should be enabled or disabled, without the view controller having to
     * know the game's internal phase. It reports the current state right
     * away when registered (useful when loading a saved game that was
     * already in the PLAYING phase).
     */
    public void setOnVerificationAvailabilityChanged(Consumer<Boolean> callback) {
        this.onVerificationAvailabilityChanged = callback != null ? callback : available -> { };
        this.onVerificationAvailabilityChanged.accept(canVerify());
    }

    /**
     * Lets the view disable "Rotate ship" and "Place random fleet" as soon
     * as the placement phase ends (usability heuristic "error prevention":
     * without this, those buttons stayed active but had no effect during
     * the game, a click that does nothing and doesn't explain why). It
     * reports the current state right away when registered, just like
     * {@link #setOnVerificationAvailabilityChanged}.
     */
    public void setOnPlacementAvailabilityChanged(Consumer<Boolean> callback) {
        this.onPlacementAvailabilityChanged = callback != null ? callback : available -> { };
        this.onPlacementAvailabilityChanged.accept(phase == GamePhase.PLACEMENT);
    }

    /**
     * Releases this game's resources (the clock thread) so it is possible
     * to go back to the main menu without leaving an orphan thread updating
     * a view that is no longer on screen (usability heuristic "user control
     * and freedom": leaving the current game without having to close the
     * whole application).
     */
    public void shutdown() {
        gameClock.stop();
    }

    /**
     * Invoked by the "Place random fleet" button: a shortcut so ships don't
     * have to be placed one by one. It only places at random the ships
     * still missing from the queue (the ones already placed by hand stay
     * where they are): if it called RandomFleetPlacer.placeRandomFleet
     * directly, that creates a brand-new fleet of 10 ships and would add
     * them on top of the ones already placed, leaving more than 10 ships
     * on the board.
     */
    public void placeRandomFleet() {
        if (phase != GamePhase.PLACEMENT) {
            return;
        }
        List<Ship> remainingShips = new ArrayList<>();
        for (ShipType type : placementQueue) {
            remainingShips.add(ShipFactory.create(type));
        }
        placementQueue.clear();
        RandomFleetPlacer.placeShipsRandomly(humanPositionBoard, remainingShips);
        positionView.redrawAll();
        startGamePhase();
    }

    private void onPositionBoardClick(Coordinate coordinate) {
        if (phase != GamePhase.PLACEMENT || placementQueue.isEmpty()) {
            return;
        }
        ShipType type = placementQueue.peekFirst();
        Ship ship = ShipFactory.create(type);
        try {
            humanPositionBoard.placeShip(ship, coordinate, currentOrientation);
            placementQueue.pollFirst();
            positionView.clearPreview();
            if (placementQueue.isEmpty()) {
                startGamePhase();
            } else {
                updatePlacementStatus();
            }
            autoSave();
        } catch (InvalidPlacementException e) {
            statusLabel.setText("Colocacion invalida: " + e.getMessage());
        }
    }

    /**
     * Previews, while the mouse moves over the placement board during the
     * placement phase, which cells the current ship in the queue would
     * occupy if placed there (green if it is a valid position, red if it
     * falls outside the board or overlaps another ship). It is just a
     * visual aid: nothing is placed until the player clicks.
     */
    private void onPositionBoardHover(Coordinate coordinate) {
        if (phase != GamePhase.PLACEMENT || placementQueue.isEmpty()) {
            return;
        }
        ShipType type = placementQueue.peekFirst();
        List<Coordinate> positions = new ArrayList<>(type.getSize());
        boolean valid = true;
        for (int i = 0; i < type.getSize(); i++) {
            Coordinate position = coordinate.shift(currentOrientation, i);
            positions.add(position);
            if (!position.isWithinBoard(Board.SIZE) || humanPositionBoard.getCell(position).hasShip()) {
                valid = false;
            }
        }
        positionView.showPlacementPreview(positions, type, currentOrientation, valid);
    }

    private void updatePlacementStatus() {
        if (placementQueue.isEmpty()) {
            return;
        }
        ShipType next = placementQueue.peekFirst();
        statusLabel.setText("Coloca tu " + next + " (" + next.getSize()
                + " casillas) - Orientacion actual: " + currentOrientation);
    }

    private void startGamePhase() {
        positionView.clearPreview();
        phase = GamePhase.PLAYING;

        // Cuando el jugador coloca los barcos uno por uno (en vez de usar
        // "Colocar flota aleatoria"), Board.placeShip nunca arma un Fleet
        // (solo RandomFleetPlacer lo hace). Sin Fleet, isFleetFullySunk()
        // siempre da falso y el jugador nunca podria perder. Si todavia no
        // hay Fleet aqui, se arma leyendo los barcos que ya quedaron en las
        // celdas del tablero.
        if (humanPositionBoard.getFleet() == null) {
            humanPositionBoard.setFleet(collectPlacedFleet(humanPositionBoard));
        }

        human = new HumanPlayer(GameSession.getHumanNickname(), humanPositionBoard);

        // La flota de la maquina ya se coloca al entrar al juego
        // (setUpMachine, en el constructor); esto es solo una red de
        // seguridad por si se cargo una partida guardada antigua que quedo
        // en fase de colocacion antes de ese cambio.
        if (machine == null) {
            setUpMachine();
        }

        turn = GameTurn.HUMAN;
        statusLabel.setText("Flota lista. Es tu turno: dispara en el tablero enemigo.");
        onVerificationAvailabilityChanged.accept(canVerify());
        onPlacementAvailabilityChanged.accept(false);
        gameClock.start();
        autoSave();
    }

    /**
     * Walks the board's 100 cells and groups (without duplicates) the
     * ships already placed on it, to build the Fleet the board is missing
     * when placement was done manually, cell by cell.
     */
    private Fleet collectPlacedFleet(Board board) {
        Set<Ship> ships = new LinkedHashSet<>();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                Ship ship = board.getCell(new Coordinate(row, column)).getShip();
                if (ship != null) {
                    ships.add(ship);
                }
            }
        }
        return new Fleet(new ArrayList<>(ships));
    }

    private void onMainBoardClick(Coordinate coordinate) {
        if (phase != GamePhase.PLAYING || turn != GameTurn.HUMAN) {
            return;
        }
        try {
            ShotResult result = machineMainBoard.shoot(coordinate);
            human.recordShot(new Shot(coordinate, result));
            statusLabel.setText("Disparaste en " + coordinate + ": " + result);

            if (machineMainBoard.isFleetFullySunk()) {
                endGame("Hundiste toda la flota enemiga. Ganaste.", true);
                return;
            }

            if (result == ShotResult.WATER) {
                turn = GameTurn.MACHINE;
                pauseThen(this::machineTurn);
            }
            // si fue TOCADO o HUNDIDO (y no gano todavia), el humano dispara de nuevo
            autoSave();
        } catch (InvalidShotException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    private void machineTurn() {
        Coordinate target = machine.chooseTarget(humanPositionBoard);
        ShotResult result = humanPositionBoard.shoot(target);
        machine.recordShot(new Shot(target, result));
        statusLabel.setText("La maquina disparo en " + target + ": " + result);

        if (humanPositionBoard.isFleetFullySunk()) {
            endGame("La maquina hundio toda tu flota. Perdiste.", false);
            return;
        }

        if (result == ShotResult.WATER) {
            turn = GameTurn.HUMAN;
            autoSave();
        } else {
            autoSave();
            pauseThen(this::machineTurn);
        }
    }

    /**
     * Small pause between the machine's shots so they can be seen in the
     * UI. Runs on a separate thread (instead of a PauseTransition, which
     * runs inside the JavaFX thread itself) so the "machine's turn" is real
     * concurrency; the action that resumes the game is sent back to the
     * JavaFX thread with {@link Platform#runLater}, since it is the only
     * one allowed to touch the Board/the views.
     */
    private void pauseThen(Runnable action) {
        Thread turnThread = new Thread(() -> {
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            Platform.runLater(action);
        }, "machine-turn");
        turnThread.setDaemon(true);
        turnThread.start();
    }

    private void endGame(String message, boolean won) {
        phase = GamePhase.FINISHED;
        statusLabel.setText(message);
        onVerificationAvailabilityChanged.accept(canVerify());
        gameClock.stop();
        autoSave();
        showGameOverDialog(message, won);
    }

    /**
     * Pantalla de fin de partida (ademas del statusLabel, que solo cambia
     * un texto discreto que podria pasar desapercibido): confirma de forma
     * clara e inequivoca que la partida termino y si se gano o se perdio,
     * reemplazando la escena del tablero por completo (no una ventana
     * flotante encima) para que el unico camino de vuelta sea su propio
     * boton "Volver al menu" (GameOverController), igual que cualquier
     * otra transicion de pantalla en la aplicacion.
     */
    private void showGameOverDialog(String message, boolean won) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/batallanaval/view/game-over-view.fxml"));
            Parent root = loader.load();
            GameOverController controller = loader.getController();
            controller.setContent(won ? "¡Ganaste!" : "Perdiste", message);

            Stage stage = (Stage) statusLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1320, 880);
            SoundManager.attachButtonSounds(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo mostrar la pantalla de fin de partida.", e);
        }
    }
}
