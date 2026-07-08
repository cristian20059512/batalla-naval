package com.batallanaval.util;

import java.io.Serializable;

/**
 * Estado en el que puede encontrarse una celda del tablero en cualquier
 * momento de la partida.
 */
public enum CellState implements Serializable {
    /** No se ha disparado y no hay barco (o el jugador no lo sabe aun). */
    EMPTY,
    /** Hay un barco propio colocado, sin disparos (solo visible en tablero de posicion). */
    SHIP,
    /** Se disparo y no habia barco. */
    WATER,
    /** Se disparo y habia barco, pero el barco no esta completamente hundido. */
    HIT,
    /** Se disparo y esa parte pertenece a un barco ya completamente hundido. */
    SUNK
}
