package com.batallanaval.util;

import java.io.Serializable;

/**
 * Resultado inmediato de haber disparado sobre una coordenada del tablero.
 */
public enum ShotResult implements Serializable {
    WATER,
    HIT,
    SUNK
}
