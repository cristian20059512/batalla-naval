package com.batallanaval.model;

import com.batallanaval.util.Coordinate;
import com.batallanaval.util.CellState;

import java.io.Serializable;

/**
 * Una casilla del tablero. Guarda su posicion, su estado visible actual y
 * una referencia opcional al barco que ocupa (null si esta vacia o es agua).
 */
public class Cell implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Coordinate coordinate;
    private CellState state;
    private Ship ship;

    public Cell(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.state = CellState.EMPTY;
        this.ship = null;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public CellState getState() {
        return state;
    }

    public void setState(CellState state) {
        this.state = state;
    }

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public boolean hasShip() {
        return ship != null;
    }

    public boolean wasAlreadyShot() {
        return state == CellState.WATER || state == CellState.HIT || state == CellState.SUNK;
    }
}
