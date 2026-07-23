package com.batallanaval.util;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a (row, column) position within the 10x10 board.
 * It is immutable and works as a key in collections (equals/hashCode defined).
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
     * Returns the coordinate "steps" cells away in the given direction,
     * useful for walking a ship cell by cell regardless of which way it
     * points (up, down, left, or right).
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
