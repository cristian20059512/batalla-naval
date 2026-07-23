package com.batallanaval.model;

import com.batallanaval.util.Coordinate;
import com.batallanaval.util.Orientation;
import com.batallanaval.util.ShipType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipTest {

    @Test
    void placeAssignsPositionsAccordingToOrientation() {
        Destroyer destroyer = new Destroyer();
        destroyer.place(new Coordinate(2, 2), Orientation.RIGHT);

        assertEquals(2, destroyer.getPositions().size());
        assertEquals(new Coordinate(2, 2), destroyer.getPositions().get(0));
        assertEquals(new Coordinate(2, 3), destroyer.getPositions().get(1));
    }

    @Test
    void occupiesCoordinateOnlyForItsOwnCells() {
        Destroyer destroyer = new Destroyer();
        destroyer.place(new Coordinate(2, 2), Orientation.RIGHT);

        assertTrue(destroyer.occupiesCoordinate(new Coordinate(2, 2)));
        assertTrue(destroyer.occupiesCoordinate(new Coordinate(2, 3)));
        assertFalse(destroyer.occupiesCoordinate(new Coordinate(2, 4)));
        assertFalse(destroyer.occupiesCoordinate(new Coordinate(3, 2)));
    }

    @Test
    void isNotSunkUntilEveryCellIsHit() {
        Destroyer destroyer = new Destroyer();
        destroyer.place(new Coordinate(0, 0), Orientation.RIGHT);

        assertFalse(destroyer.isSunk());

        destroyer.receiveHit(new Coordinate(0, 0));
        assertFalse(destroyer.isSunk(), "un barco de 2 casillas no deberia hundirse con un solo impacto");

        destroyer.receiveHit(new Coordinate(0, 1));
        assertTrue(destroyer.isSunk());
    }

    @Test
    void singleCellShipSinksWithOneHit() {
        Frigate frigate = new Frigate();
        frigate.place(new Coordinate(5, 5), Orientation.RIGHT);

        assertFalse(frigate.isSunk());
        frigate.receiveHit(new Coordinate(5, 5));
        assertTrue(frigate.isSunk());
    }

    @Test
    void repeatedHitsOnSameCellDoNotSinkShipEarly() {
        Destroyer destroyer = new Destroyer();
        destroyer.place(new Coordinate(0, 0), Orientation.RIGHT);

        destroyer.receiveHit(new Coordinate(0, 0));
        destroyer.receiveHit(new Coordinate(0, 0));

        assertFalse(destroyer.isSunk(), "impactar la misma casilla repetida no deberia contar dos veces");
    }

    @Test
    void typeAndSizeMatchTheShipKind() {
        AircraftCarrier carrier = new AircraftCarrier();
        assertEquals(ShipType.AIRCRAFT_CARRIER, carrier.getType());
        assertEquals(4, carrier.getSize());

        Submarine submarine = new Submarine();
        assertEquals(ShipType.SUBMARINE, submarine.getType());
        assertEquals(3, submarine.getSize());
    }
}
