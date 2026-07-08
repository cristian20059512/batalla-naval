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
 * Tablero de 10x10 casillas. Contiene la logica de negocio de colocacion de
 * barcos (HU-1) y de disparos (HU-2/HU-4): validaciones, deteccion de
 * agua/tocado/hundido, y notificacion a la vista via el patron Observer
 * (ver {@link BoardListener}).
 *
 * La misma clase sirve tanto para el tablero de posicion del jugador humano
 * como para el tablero principal (propio de la maquina o del humano segun
 * quien dispare), ya que ambos son, en esencia, un grid 10x10 con una flota.
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
     * Coloca un barco en el tablero validando las reglas de HU-1: dentro
     * del tablero y sin superposicion con otro barco ya colocado.
     * Una vez colocado, el barco no puede volver a colocarse (no se provee
     * metodo de "mover"), cumpliendo la definicion de hecho de HU-1.
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
     * Procesa un disparo sobre la coordenada indicada (HU-2/HU-4).
     * Devuelve el resultado (AGUA, TOCADO o HUNDIDO) y notifica a los
     * listeners registrados para que la vista se actualice en tiempo real.
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
     * Cuando un barco se hunde, todas sus casillas (incluidas las que ya
     * estaban en TOCADO) pasan a mostrarse como HUNDIDO, tal como pide el
     * enunciado ("aparecera en el tablero el barco completo con la marca
     * indicativa de que ha sido hundido").
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
     * Copia el estado (celdas y flota) de un tablero recien deserializado
     * dentro de este tablero ya construido, en vez de reemplazar la
     * instancia. Asi los listeners ya registrados (la vista JavaFX) y la
     * referencia final a este {@code Board} siguen siendo validos despues
     * de cargar una partida guardada.
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
