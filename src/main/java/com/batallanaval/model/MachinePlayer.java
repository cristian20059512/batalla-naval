package com.batallanaval.model;

import com.batallanaval.ai.ShootingStrategy;
import com.batallanaval.util.Coordinate;

/**
 * Machine player. Its own board is the opponent's "main board": it is
 * generated automatically (fleet placed at random) and is where the human
 * player fires. It delegates choosing its own shots (against the human's
 * board) to a {@link ShootingStrategy}, which allows changing its behavior
 * without touching this class (Strategy pattern).
 */
public class MachinePlayer extends Player {

    private static final long serialVersionUID = 1L;

    private final ShootingStrategy shootingStrategy;

    public MachinePlayer(String nickname, Board mainBoard, ShootingStrategy shootingStrategy) {
        super(nickname, mainBoard);
        this.shootingStrategy = shootingStrategy;
    }

    /**
     * Chooses the coordinate where the machine will fire within the human
     * player's board, using the configured strategy.
     */
    public Coordinate chooseTarget(Board humanBoard) {
        return shootingStrategy.chooseShot(humanBoard);
    }
}
