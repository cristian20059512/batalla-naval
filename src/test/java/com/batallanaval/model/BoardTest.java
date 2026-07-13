package com.batallanaval.model;

import com.batallanaval.exception.InvalidPlacementException;
import com.batallanaval.exception.InvalidShotException;
import com.batallanaval.util.CellState;
import com.batallanaval.util.Coordinate;
import com.batallanaval.util.Orientation;
import com.batallanaval.util.ShotResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    @Test
    void placingAShipMarksItsCellsAsShip() throws InvalidPlacementException {
        Board board = new Board();
        Frigate frigate = new Frigate();

        board.placeShip(frigate, new Coordinate(4, 4), Orientation.RIGHT);

        Cell cell = board.getCell(new Coordinate(4, 4));
        assertTrue(cell.hasShip());
        assertEquals(CellState.SHIP, cell.getState());
    }

    @Test
    void placingAShipOutsideTheBoardThrows() {
        Board board = new Board();
        AircraftCarrier carrier = new AircraftCarrier();

        assertThrows(InvalidPlacementException.class,
                () -> board.placeShip(carrier, new Coordinate(0, 8), Orientation.RIGHT));
    }

    @Test
    void placingAShipOverlappingAnotherThrows() throws InvalidPlacementException {
        Board board = new Board();
        board.placeShip(new Frigate(), new Coordinate(2, 2), Orientation.RIGHT);

        Destroyer destroyer = new Destroyer();
        assertThrows(InvalidPlacementException.class,
                () -> board.placeShip(destroyer, new Coordinate(2, 1), Orientation.RIGHT));
    }

    @Test
    void shootingAnEmptyCellReturnsWaterAndMarksIt() {
        Board board = new Board();

        ShotResult result = board.shoot(new Coordinate(1, 1));

        assertEquals(ShotResult.WATER, result);
        assertEquals(CellState.WATER, board.getCell(new Coordinate(1, 1)).getState());
    }

    @Test
    void shootingASingleCellShipSinksItImmediately() throws InvalidPlacementException {
        Board board = new Board();
        board.placeShip(new Frigate(), new Coordinate(6, 6), Orientation.RIGHT);

        ShotResult result = board.shoot(new Coordinate(6, 6));

        assertEquals(ShotResult.SUNK, result);
        assertEquals(CellState.SUNK, board.getCell(new Coordinate(6, 6)).getState());
    }

    @Test
    void shootingTheSameCellTwiceThrows() {
        Board board = new Board();
        Coordinate target = new Coordinate(3, 3);
        board.shoot(target);

        assertThrows(InvalidShotException.class, () -> board.shoot(target));
    }

    @Test
    void shootingOutsideTheBoardThrows() {
        Board board = new Board();
        assertThrows(InvalidShotException.class, () -> board.shoot(new Coordinate(20, 20)));
    }

    @Test
    void listenersAreNotifiedWhenACellChanges() {
        Board board = new Board();
        List<Coordinate> notified = new ArrayList<>();
        board.addListener((coordinate, state) -> notified.add(coordinate));

        board.shoot(new Coordinate(0, 0));

        assertEquals(1, notified.size());
        assertEquals(new Coordinate(0, 0), notified.get(0));
    }

    @Test
    void isFleetFullySunkReflectsTheUnderlyingShips() throws InvalidPlacementException {
        Board board = new Board();
        Frigate frigate = new Frigate();
        board.placeShip(frigate, new Coordinate(7, 7), Orientation.RIGHT);
        board.setFleet(new Fleet(List.of(frigate)));

        assertFalse(board.isFleetFullySunk());

        board.shoot(new Coordinate(7, 7));

        assertTrue(board.isFleetFullySunk());
    }

    @Test
    void restoreStateCopiesCellsAndFleetFromAnotherBoard() throws InvalidPlacementException {
        Board source = new Board();
        Frigate frigate = new Frigate();
        source.placeShip(frigate, new Coordinate(8, 8), Orientation.RIGHT);
        source.setFleet(new Fleet(List.of(frigate)));

        Board target = new Board();
        target.restoreState(source);

        assertTrue(target.getCell(new Coordinate(8, 8)).hasShip());
        assertEquals(CellState.SHIP, target.getCell(new Coordinate(8, 8)).getState());
        assertEquals(1, target.getFleet().getTotalShips());
    }
}
