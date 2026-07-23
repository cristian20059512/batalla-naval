package com.batallanaval.util;

import java.io.Serializable;

/**
 * State a board cell can be in at any point during the game.
 */
public enum CellState implements Serializable {
    /** No shot has been fired and there is no ship (or the player doesn't know yet). */
    EMPTY,
    /** An own ship is placed here, with no shots fired (only visible on the placement board). */
    SHIP,
    /** A shot was fired and there was no ship. */
    WATER,
    /** A shot was fired and there was a ship, but it is not fully sunk yet. */
    HIT,
    /** A shot was fired and this part belongs to a ship that is already fully sunk. */
    SUNK
}
