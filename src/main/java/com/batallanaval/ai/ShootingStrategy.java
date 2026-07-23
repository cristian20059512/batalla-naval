package com.batallanaval.ai;

import com.batallanaval.model.Board;
import com.batallanaval.util.Coordinate;

/**
 * Strategy pattern: defines how the machine chooses its next shooting
 * coordinate. Allows changing the AI's behavior (for example, from purely
 * random to one that "hunts" around a recent hit) without touching
 * {@link com.batallanaval.model.MachinePlayer} or the controller.
 */
public interface ShootingStrategy {

    /**
     * Chooses the next shooting coordinate on the opponent's board,
     * guaranteeing it is not a cell that was already fired at.
     */
    Coordinate chooseShot(Board enemyBoard);
}
