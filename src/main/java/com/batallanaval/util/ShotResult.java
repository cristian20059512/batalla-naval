package com.batallanaval.util;

import java.io.Serializable;

/**
 * Immediate result of firing at a coordinate on the board.
 */
public enum ShotResult implements Serializable {
    WATER,
    HIT,
    SUNK
}
