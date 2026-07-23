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
import com.batallanaval.view.Board3DView;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

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
 * Orquesta la partida completa: fase de colocacion (HU-1), turnos de
 * disparo humano/maquina (HU-2/HU-4), verificacion del tablero enemigo
 * (HU-3) y deteccion de victoria. Es el "Controlador" de la arquitectura
 * MVC: conoce el Modelo (Board, Player) y la Vista (Board3DView), pero la
 * Vista y el Modelo no se conocen entre si directamente (salvo por el
 * Observer BoardListener, que ya desacopla esa relacion).
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
     * Coloca la flota de la maquina y crea el MachinePlayer desde que se
     * entra al juego, no solo cuando el humano termina de colocar la suya:
     * asi el tablero enemigo ya tiene barcos si se usa la brujula para
     * verificarlo antes de empezar a disparar, en vez de verse vacio.
     */
    private void setUpMachine() {
        RandomFleetPlacer.placeRandomFleet(machineMainBoard);
        machine = new MachinePlayer("Maquina", machineMainBoard, new HuntTargetShootingStrategy());
        mainView.redrawAll();
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
     * Guardado automatico (HU de persistencia): guarda el estado completo
     * de los tableros (archivo serializable) y un resumen legible con
     * nickname y barcos hundidos (archivo plano) cada vez que la partida
     * cambia de estado. Un fallo de E/S no debe interrumpir el juego, solo
     * se registra en el log.
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

    /**
     * Invocado por el boton brujula (modo de verificacion, HU-3). Devuelve
     * el nuevo estado para que la vista pueda reflejarlo (p. ej. resaltando
     * el boton mientras el modo esta activo).
     *
     * NOTA: el enunciado (HU-3) pide que esta opcion sea "unicamente para
     * fines de verificacion y no [este disponible] durante el juego
     * normal". Por pedido explicito del usuario se dejo sin esa
     * restriccion (canVerify() ya no revisa la fase): el boton se puede
     * activar/desactivar en cualquier momento, aunque eso se aparte de la
     * letra de esa historia de usuario.
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

    /** Estado actual del modo de verificacion, para que la vista lo refleje al cargar una partida guardada. */
    public boolean isVerificationMode() {
        return verificationMode;
    }

    /**
     * Siempre disponible (ver nota en {@link #toggleVerification()}). Se
     * deja el metodo (en vez de eliminarlo) para no desarmar el mecanismo
     * de aviso a la vista (setOnVerificationAvailabilityChanged) por si se
     * decide restaurar la restriccion mas adelante.
     */
    private boolean canVerify() {
        return true;
    }

    /**
     * Permite que la vista (MainController) se entere de cuando el boton de
     * verificacion debe habilitarse o deshabilitarse, sin que el controlador
     * de la vista tenga que conocer la fase interna de la partida. Se avisa
     * de inmediato con el estado actual al registrarse (util al cargar una
     * partida guardada que ya estaba en fase JUEGO).
     */
    public void setOnVerificationAvailabilityChanged(Consumer<Boolean> callback) {
        this.onVerificationAvailabilityChanged = callback != null ? callback : available -> { };
        this.onVerificationAvailabilityChanged.accept(canVerify());
    }

    /**
     * Permite que la vista deshabilite "Girar barco" y "Colocar flota
     * aleatoria" en cuanto termina la fase de colocacion (heuristica de
     * usabilidad "prevencion de errores": sin esto, esos botones quedaban
     * activos pero sin efecto durante la partida, un clic que no hace nada
     * y no explica por que). Se avisa de inmediato con el estado actual al
     * registrarse, igual que {@link #setOnVerificationAvailabilityChanged}.
     */
    public void setOnPlacementAvailabilityChanged(Consumer<Boolean> callback) {
        this.onPlacementAvailabilityChanged = callback != null ? callback : available -> { };
        this.onPlacementAvailabilityChanged.accept(phase == GamePhase.PLACEMENT);
    }

    /**
     * Libera los recursos de esta partida (el hilo del cronometro) para que
     * se pueda volver al menu principal sin dejar un hilo huerfano
     * actualizando una vista que ya no esta en pantalla (heuristica de
     * usabilidad "control y libertad del usuario": salir de la partida en
     * curso sin tener que cerrar toda la aplicacion).
     */
    public void shutdown() {
        gameClock.stop();
    }

    /**
     * Invocado por el boton "Colocar flota aleatoria": atajo para no
     * colocar barco por barco. Solo coloca al azar los barcos que todavia
     * faltan en la cola (los ya puestos a mano se quedan donde estan): si
     * llamara a RandomFleetPlacer.placeRandomFleet directamente, este crea
     * una flota nueva de 10 barcos y los agregaria encima de los que ya
     * estaban colocados, dejando mas de 10 barcos en el tablero.
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
     * Recorre las 100 casillas del tablero y agrupa (sin duplicados) los
     * barcos que ya estan colocados en el, para armar el Fleet que le hace
     * falta al tablero cuando la colocacion fue manual, casilla por casilla.
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

    /**
     * Pequena pausa entre disparos de la maquina para que se alcancen a ver
     * en la UI. Corre en un hilo aparte (en vez de un PauseTransition, que
     * se ejecuta dentro del propio hilo de JavaFX) para que el "turno de la
     * maquina" sea concurrencia real; la accion que retoma el juego se
     * reenvia al hilo de JavaFX con {@link Platform#runLater}, ya que es el
     * unico autorizado a tocar el Board/las vistas.
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

    private void endGame(String message) {
        phase = GamePhase.FINISHED;
        statusLabel.setText(message);
        onVerificationAvailabilityChanged.accept(canVerify());
        gameClock.stop();
        autoSave();
    }
}
