package com.batallanaval.util;

import java.io.Serializable;

/**
 * Direction a ship extends from the cell the player clicks on (that cell is
 * always the bow). The 4 cardinal directions allow placing the ship pointing
 * to any of the 4 sides, without introducing diagonals (which the
 * assignment does not call for).
 */
public enum Orientation implements Serializable {
    RIGHT,
    DOWN,
    LEFT,
    UP
}
