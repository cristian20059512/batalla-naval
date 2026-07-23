package com.batallanaval.model;

import com.batallanaval.exception.InvalidPlacementException;
import com.batallanaval.exception.InvalidShotException;
import com.batallanaval.util.Coordinate;
import com.batallanaval.util.CellState;
import com.batallanaval.util.Orientation;
import com.batallanaval.util.ShotResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A 10x10 board. Holds the business logic for placing ships (HU-1) and for
 * shooting (HU-2/HU-4): validations, water/hit/sunk detection, and
 * notifying the view via the Observer pattern (see {@link BoardListener}).
 *
 * The same class serves both the human player's placement board and the
 * main board (the machine's or the human's, depending on who is firing),
 * since both are, in essence, a 10x10 grid with a fleet on it.
 */
public class Board implements Serializable {

    public static final int SIZE = 10;

    private static final long serialVersionUID = 1L;

    private final Cell[][] cells;
    private Fleet fleet;

    // transient: los listeners (controladores JavaFX) no se serializan;
    // se vuelven a registrar al recargar una partida guardada.
    private final transient List<BoardListener> listeners = new ArrayList<>();

    public Board() {
        cells = new Cell[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                cells[row][column] = new Cell(new Coordinate(row, column));
            }
        }
    }

    public void setFleet(Fleet fleet) {
        this.fleet = fleet;
    }

    public Fleet getFleet() {
        return fleet;
    }

    public Cell getCell(Coordinate coordinate) {
        return cells[coordinate.getRow()][coordinate.getColumn()];
    }

    public void addListener(BoardListener listener) {
        listeners.add(listener);
    }

    public void removeListener(BoardListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Coordinate coordinate, CellState state) {
        for (BoardListener listener : listeners) {
            listener.onCellChanged(coordinate, state);
        }
    }

    /**
     * Places a ship on the board, validating HU-1's rules: within the board
     * and without overlapping another ship already placed.
     * Once placed, a ship cannot be placed again (no "move" method is
     * provided), meeting HU-1's definition of done.
     */
    public void placeShip(Ship ship, Coordinate start, Orientation orientation)
            throws InvalidPlacementException {

        List<Coordinate> positions = calculatePositions(ship, start, orientation);

        for (Coordinate coordinate : positions) {
            if (!coordinate.isWithinBoard(SIZE)) {
                throw new InvalidPlacementException(
                        "El barco " + ship.getName() + " queda fuera del tablero en " + coordinate);
            }
            if (getCell(coordinate).hasShip()) {
                throw new InvalidPlacementException(
                        "El barco " + ship.getName() + " se superpone con otro barco en " + coordinate);
            }
        }

        ship.place(start, orientation);
        for (Coordinate coordinate : positions) {
            Cell cell = getCell(coordinate);
            cell.setShip(ship);
            cell.setState(CellState.SHIP);
            notifyListeners(coordinate, CellState.SHIP);
        }
    }

    private List<Coordinate> calculatePositions(Ship ship, Coordinate start, Orientation orientation) {
        List<Coordinate> positions = new ArrayList<>(ship.getSize());
        for (int i = 0; i < ship.getSize(); i++) {
            positions.add(start.shift(orientation, i));
        }
        return positions;
    }

    /**
     * Processes a shot at the given coordinate (HU-2/HU-4).
     * Returns the result (WATER, HIT, or SUNK) and notifies the registered
     * listeners so the view updates in real time.
     */
    public ShotResult shoot(Coordinate coordinate) {
        if (!coordinate.isWithinBoard(SIZE)) {
            throw new InvalidShotException("La coordenada " + coordinate + " esta fuera del tablero.");
        }

        Cell cell = getCell(coordinate);
        if (cell.wasAlreadyShot()) {
            throw new InvalidShotException("Ya se disparo antes en " + coordinate + ".");
        }

        ShotResult result;
        if (!cell.hasShip()) {
            cell.setState(CellState.WATER);
            result = ShotResult.WATER;
        } else {
            Ship ship = cell.getShip();
            ship.receiveHit(coordinate);
            if (ship.isSunk()) {
                cell.setState(CellState.SUNK);
                result = ShotResult.SUNK;
                markShipSunk(ship);
            } else {
                cell.setState(CellState.HIT);
                result = ShotResult.HIT;
            }
        }

        notifyListeners(coordinate, cell.getState());
        return result;
    }

    /**
     * When a ship sinks, all of its cells (including the ones already
     * marked HIT) switch to showing SUNK, as the assignment requires ("the
     * whole ship will appear on the board marked as sunk").
     */
    private void markShipSunk(Ship ship) {
        for (Coordinate coordinate : ship.getPositions()) {
            Cell cell = getCell(coordinate);
            cell.setState(CellState.SUNK);
            notifyListeners(coordinate, CellState.SUNK);
        }
    }

    public boolean isFleetFullySunk() {
        return fleet != null && fleet.isCompletelySunk();
    }

    /**
     * Copies the state (cells and fleet) of a freshly deserialized board
     * into this already-constructed board, instead of replacing the
     * instance. This way, the listeners already registered (the JavaFX
     * view) and the final reference to this {@code Board} remain valid
     * after loading a saved game.
     */
    public void restoreState(Board source) {
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                Coordinate coordinate = new Coordinate(row, column);
                Cell sourceCell = source.getCell(coordinate);
                Cell targetCell = getCell(coordinate);
                targetCell.setShip(sourceCell.getShip());
                targetCell.setState(sourceCell.getState());
            }
        }
        this.fleet = source.getFleet();
    }
}
