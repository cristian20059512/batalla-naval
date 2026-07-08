package com.batallanaval.controller;

import com.batallanaval.ai.HuntTargetShootingStrategy;
import com.batallanaval.exception.InvalidPlacementException;
import com.batallanaval.exception.InvalidShotException;
import com.batallanaval.exception.PersistenceException;
import com.batallanaval.model.Board;
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
import com.batallanaval.util.GamePhase;
import com.batallanaval.util.Orientation;
import com.batallanaval.util.ShotResult;
import com.batallanaval.util.GameSession;
import com.batallanaval.util.ShipType;
import com.batallanaval.util.GameTurn;
import com.batallanaval.view.BoardView;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orquesta la partida completa: fase de colocacion (HU-1), turnos de
 * disparo humano/maquina (HU-2/HU-4), verificacion del tablero enemigo
 * (HU-3) y deteccion de victoria. Es el "Controlador" de la arquitectura
 * MVC: conoce el Modelo (Board, Player) y la Vista (BoardView), pero la
 * Vista y el Modelo no se conocen entre si directamente (salvo por el
 * Observer BoardListener, que ya desacopla esa relacion).
 */
public class GameController {

    private static final Logger LOG = Logger.getLogger(GameController.class.getName());

    private final Board humanPositionBoard = new Board();
    private final Board machineMainBoard = new Board();

    private final BoardView positionView;
    private final BoardView mainView;
    private final Label statusLabel;

    private final GamePersistenceManager persistenceManager = new GamePersistenceManager();

    private HumanPlayer human;
    private MachinePlayer machine;

    private GamePhase phase = GamePhase.PLACEMENT;
    private GameTurn turn = GameTurn.HUMAN;

    private final List<ShipType> placementQueue = new ArrayList<>();
    private Orientation currentOrientation = Orientation.RIGHT;
    private boolean verificationMode = false;

    public GameController(Pane positionContainer, Pane mainContainer, Label statusLabel) {
        this.statusLabel = statusLabel;

        positionView = new BoardView(humanPositionBoard, true);
        mainView = new BoardView(machineMainBoard, false);

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
        }
    }

    /**
     * Reconstruye la partida a partir de los archivos de guardado (HU de
     * persistencia): copia el estado de los tableros deserializados dentro
     * de los tableros ya construidos (para no invalidar los listeners de
     * la vista) y retoma fase/turno/cola de colocacion tal como quedaron.
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
                return;
            }

            human = new HumanPlayer(summary.getHumanNickname(), humanPositionBoard);
            machine = new MachinePlayer(summary.getMachineNickname(), machineMainBoard,
                    new HuntTargetShootingStrategy());

            if (phase == GamePhase.FINISHED) {
                statusLabel.setText("Partida cargada: la partida ya habia finalizado.");
            } else {
                statusLabel.setText("Partida cargada. Turno: " + turn);
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
     * Guardado automatico (HU de persistencia): guarda el estado completo
     * de los tableros (archivo serializable) y un resumen legible con
     * nickname y barcos hundidos (archivo plano) cada vez que la partida
     * cambia de estado. Un fallo de E/S no debe interrumpir el juego, solo
     * se registra en el log.
     */
    private void autoSave() {
        GameState state = new GameState(humanPositionBoard, machineMainBoard, phase, turn,
                placementQueue, currentOrientation, verificationMode);
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
     * Invocado por el boton "Girar barco" (o la tecla R): rota la
     * orientacion 90 grados en sentido horario cada vez (derecha, abajo,
     * izquierda, arriba, y vuelve a derecha), para poder apuntar el barco
     * hacia cualquiera de los 4 lados antes de colocarlo.
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

    /** Invocado por el boton "Ver tablero enemigo": modo de verificacion (HU-3). */
    public void toggleVerification() {
        verificationMode = !verificationMode;
        mainView.setVerificationMode(verificationMode);
    }

    /** Invocado por el boton "Colocar flota aleatoria": atajo para no colocar barco por barco. */
    public void placeRandomFleet() {
        if (phase != GamePhase.PLACEMENT) {
            return;
        }
        placementQueue.clear();
        RandomFleetPlacer.placeRandomFleet(humanPositionBoard);
        positionView.redrawAll();
        startGamePhase();
    }

    private void onPositionBoardClick(Coordinate coordinate) {
        if (phase != GamePhase.PLACEMENT || placementQueue.isEmpty()) {
            return;
        }
        ShipType type = placementQueue.get(0);
        Ship ship = ShipFactory.create(type);
        try {
            humanPositionBoard.placeShip(ship, coordinate, currentOrientation);
            placementQueue.remove(0);
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
     * Previsualiza, mientras el mouse pasa sobre el tablero de posicion
     * durante la fase de colocacion, que casillas ocuparia el barco actual
     * de la cola si se colocara ahi (en verde si es una posicion valida, en
     * rojo si queda fuera del tablero o se superpone con otro barco). Es
     * solo una ayuda visual: no coloca nada hasta que el jugador hace clic.
     */
    private void onPositionBoardHover(Coordinate coordinate) {
        if (phase != GamePhase.PLACEMENT || placementQueue.isEmpty()) {
            return;
        }
        ShipType type = placementQueue.get(0);
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
        ShipType next = placementQueue.get(0);
        statusLabel.setText("Coloca tu " + next + " (" + next.getSize()
                + " casillas) - Orientacion actual: " + currentOrientation);
    }

    private void startGamePhase() {
        positionView.clearPreview();
        phase = GamePhase.PLAYING;
        human = new HumanPlayer(GameSession.getHumanNickname(), humanPositionBoard);

        RandomFleetPlacer.placeRandomFleet(machineMainBoard);
        machine = new MachinePlayer("Maquina", machineMainBoard, new HuntTargetShootingStrategy());
        mainView.redrawAll();

        turn = GameTurn.HUMAN;
        statusLabel.setText("Flota lista. Es tu turno: dispara en el tablero enemigo.");
        autoSave();
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
                endGame("Hundiste toda la flota enemiga. Ganaste.");
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
            endGame("La maquina hundio toda tu flota. Perdiste.");
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

    /** Pequena pausa entre disparos de la maquina para que se alcancen a ver en la UI. */
    private void pauseThen(Runnable action) {
        PauseTransition pause = new PauseTransition(Duration.seconds(0.6));
        pause.setOnFinished(event -> action.run());
        pause.play();
    }

    private void endGame(String message) {
        phase = GamePhase.FINISHED;
        statusLabel.setText(message);
        autoSave();
    }
}
