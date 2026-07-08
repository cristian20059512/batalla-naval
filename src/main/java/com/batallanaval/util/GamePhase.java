package com.batallanaval.util;

import java.io.Serializable;

/**
 * Fase en la que se encuentra la partida. Se extrajo de GameController (antes
 * era un enum privado) para poder guardarla como parte del estado
 * serializable de la partida.
 */
public enum GamePhase implements Serializable {
    PLACEMENT,
    PLAYING,
    FINISHED
}
