package com.batallanaval.persistence;

import com.batallanaval.model.Board;
import com.batallanaval.util.GamePhase;
import com.batallanaval.util.Orientation;
import com.batallanaval.util.ShipType;
import com.batallanaval.util.GameTurn;

import java.io.Serializable;
import java.util.List;

/**
 * Fotografia serializable de todo lo necesario para reanudar la partida:
 * los dos tableros completos (con flotas, celdas e impactos ya aplicados) y
 * el estado de turno/fase/colocacion del {@code GameController}. Es lo que
 * el adaptador de serializacion guarda en el archivo binario.
 */
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Board humanPositionBoard;
    private final Board machineMainBoard;
    private final GamePhase phase;
    private final GameTurn turn;
    private final List<ShipType> placementQueue;
    private final Orientation currentOrientation;
    private final boolean verificationMode;

    public GameState(Board humanPositionBoard, Board machineMainBoard, GamePhase phase,
                      GameTurn turn, List<ShipType> placementQueue, Orientation currentOrientation,
                      boolean verificationMode) {
        this.humanPositionBoard = humanPositionBoard;
        this.machineMainBoard = machineMainBoard;
        this.phase = phase;
        this.turn = turn;
        this.placementQueue = placementQueue;
        this.currentOrientation = currentOrientation;
        this.verificationMode = verificationMode;
    }

    public Board getHumanPositionBoard() {
        return humanPositionBoard;
    }

    public Board getMachineMainBoard() {
        return machineMainBoard;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public GameTurn getTurn() {
        return turn;
    }

    public List<ShipType> getPlacementQueue() {
        return placementQueue;
    }

    public Orientation getCurrentOrientation() {
        return currentOrientation;
    }

    public boolean isVerificationMode() {
        return verificationMode;
    }
}
