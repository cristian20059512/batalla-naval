package com.batallanaval.ai;

import com.batallanaval.model.AircraftCarrier;
import com.batallanaval.model.Board;
import com.batallanaval.model.Fleet;
import com.batallanaval.exception.InvalidPlacementException;
import com.batallanaval.util.Coordinate;
import com.batallanaval.util.Orientation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuntTargetShootingStrategyTest {

    @Test
    void targetsOnlyCellsAdjacentToAPendingHit() throws InvalidPlacementException {
        Board board = new Board();
        AircraftCarrier carrier = new AircraftCarrier();
        board.setFleet(new Fleet(List.of(carrier)));
        board.placeShip(carrier, new Coordinate(4, 3), Orientation.RIGHT);

        // toca el barco sin hundirlo (ocupa 4 casillas, un solo golpe no basta)
        board.shoot(new Coordinate(4, 4));

        HuntTargetShootingStrategy strategy = new HuntTargetShootingStrategy();
        for (int i = 0; i < 25; i++) {
            Coordinate shot = strategy.chooseShot(board);
            boolean adjacentToHit =
                    (shot.getRow() == 4 && Math.abs(shot.getColumn() - 4) == 1)
                            || (shot.getColumn() == 4 && Math.abs(shot.getRow() - 4) == 1);
            assertTrue(adjacentToHit, "deberia elegir siempre una casilla vecina al tocado, eligio " + shot);
        }
    }

    @Test
    void neverTargetsACellThatWasAlreadyShot() throws InvalidPlacementException {
        Board board = new Board();
        AircraftCarrier carrier = new AircraftCarrier();
        board.setFleet(new Fleet(List.of(carrier)));
        board.placeShip(carrier, new Coordinate(4, 3), Orientation.RIGHT);
        board.shoot(new Coordinate(4, 4));
        board.shoot(new Coordinate(4, 5));

        HuntTargetShootingStrategy strategy = new HuntTargetShootingStrategy();
        for (int i = 0; i < 25; i++) {
            Coordinate shot = strategy.chooseShot(board);
            assertFalse(board.getCell(shot).wasAlreadyShot(),
                    "no deberia repetir un disparo en " + shot);
        }
    }

    @Test
    void fallsBackToAnyAvailableCellWhenThereIsNoPendingHit() {
        Board board = new Board();
        HuntTargetShootingStrategy strategy = new HuntTargetShootingStrategy();

        Coordinate shot = strategy.chooseShot(board);

        assertTrue(shot.isWithinBoard(Board.SIZE));
        assertFalse(board.getCell(shot).wasAlreadyShot());
    }
}
