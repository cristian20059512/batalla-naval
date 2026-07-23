package com.batallanaval.util;

import java.io.Serializable;

/**
 * Phase the game is currently in. Extracted from GameController (it used to
 * be a private enum) so it can be saved as part of the game's serializable
 * state.
 */
public enum GamePhase implements Serializable {
    PLACEMENT,
    PLAYING,
    FINISHED
}
