package com.batallanaval.util;

import java.io.Serializable;

/**
 * Tipos de barco definidos por el enunciado, con el tamano (numero de
 * casillas que ocupa) y la cantidad de unidades que tiene cada flota.
 */
public enum TipoBarco implements Serializable {
    PORTAAVIONES(4, 1),
    SUBMARINO(3, 2),
    DESTRUCTOR(2, 3),
    FRAGATA(1, 4);

    private final int tamanio;
    private final int cantidadEnFlota;

    TipoBarco(int tamanio, int cantidadEnFlota) {
        this.tamanio = tamanio;
        this.cantidadEnFlota = cantidadEnFlota;
    }

    public int getTamanio() {
        return tamanio;
    }

    public int getCantidadEnFlota() {
        return cantidadEnFlota;
    }
}
