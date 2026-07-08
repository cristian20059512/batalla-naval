package com.batallanaval.util;

import java.io.Serializable;

/**
 * Resultado inmediato de haber disparado sobre una coordenada del tablero.
 */
public enum ResultadoDisparo implements Serializable {
    AGUA,
    TOCADO,
    HUNDIDO
}
