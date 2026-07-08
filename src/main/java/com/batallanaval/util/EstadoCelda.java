package com.batallanaval.util;

import java.io.Serializable;

/**
 * Estado en el que puede encontrarse una celda del tablero en cualquier
 * momento de la partida.
 */
public enum EstadoCelda implements Serializable {
    /** No se ha disparado y no hay barco (o el jugador no lo sabe aun). */
    VACIA,
    /** Hay un barco propio colocado, sin disparos (solo visible en tablero de posicion). */
    BARCO,
    /** Se disparo y no habia barco. */
    AGUA,
    /** Se disparo y habia barco, pero el barco no esta completamente hundido. */
    TOCADO,
    /** Se disparo y esa parte pertenece a un barco ya completamente hundido. */
    HUNDIDO
}
