package com.batallanaval.util;

import java.io.Serializable;

/**
 * De quien es el turno de disparar. Se extrajo de GameController (antes era
 * un enum privado) para poder guardarla como parte del estado serializable
 * de la partida.
 */
public enum GameTurn implements Serializable {
    HUMAN,
    MACHINE
}
