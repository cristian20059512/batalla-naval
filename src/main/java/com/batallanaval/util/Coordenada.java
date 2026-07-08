package com.batallanaval.util;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa una posicion (fila, columna) dentro del tablero 10x10.
 * Es inmutable y sirve como clave en colecciones (equals/hashCode definidos).
 */
public final class Coordenada implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int fila;
    private final int columna;

    public Coordenada(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    /**
     * Devuelve la coordenada adyacente en la direccion indicada, util para
     * recorrer un barco horizontal o vertical casilla por casilla.
     */
    public Coordenada desplazar(Orientacion orientacion, int pasos) {
        if (orientacion == Orientacion.HORIZONTAL) {
            return new Coordenada(fila, columna + pasos);
        }
        return new Coordenada(fila + pasos, columna);
    }

    public boolean dentroDelTablero(int tamanioTablero) {
        return fila >= 0 && fila < tamanioTablero && columna >= 0 && columna < tamanioTablero;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordenada)) return false;
        Coordenada that = (Coordenada) o;
        return fila == that.fila && columna == that.columna;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fila, columna);
    }

    @Override
    public String toString() {
        return "(" + fila + ", " + columna + ")";
    }
}
