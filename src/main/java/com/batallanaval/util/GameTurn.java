package com.batallanaval.util;

import java.io.Serializable;

/**
 * Whose turn it is to fire. Extracted from GameController (it used to be a
 * private enum) so it can be saved as part of the game's serializable state.
 */
public enum GameTurn implements Serializable {
    HUMAN,
    MACHINE
}
