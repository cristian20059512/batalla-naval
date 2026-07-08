package com.batallanaval.util;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa una posicion (fila, columna) dentro del tablero 10x10.
 * Es inmutable y sirve como clave en colecciones (equals/hashCode definidos).
 */
public final class Coordinate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int row;
    private final int column;

    public Coordinate(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    /**
     * Devuelve la coordenada a "steps" casillas de distancia en la
     * direccion indicada, util para recorrer un barco casilla por casilla
     * sin importar hacia donde apunte (arriba, abajo, izquierda o derecha).
     */
    public Coordinate shift(Orientation orientation, int steps) {
        switch (orientation) {
            case RIGHT:
                return new Coordinate(row, column + steps);
            case LEFT:
                return new Coordinate(row, column - steps);
            case DOWN:
                return new Coordinate(row + steps, column);
            case UP:
            default:
                return new Coordinate(row - steps, column);
        }
    }

    public boolean isWithinBoard(int boardSize) {
        return row >= 0 && row < boardSize && column >= 0 && column < boardSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate)) return false;
        Coordinate that = (Coordinate) o;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + column + ")";
    }
}
