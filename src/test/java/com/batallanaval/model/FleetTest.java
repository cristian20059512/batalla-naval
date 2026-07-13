package com.batallanaval.model;

import com.batallanaval.util.Coordinate;
import com.batallanaval.util.Orientation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetTest {

    @Test
    void getTotalShipsMatchesTheListSize() {
        Fleet fleet = new Fleet(List.of(new Frigate(), new Frigate(), new Destroyer()));
        assertEquals(3, fleet.getTotalShips());
    }

    @Test
    void isCompletelySunkFalseWhileAnyShipSurvives() {
        Frigate frigate = new Frigate();
        Destroyer destroyer = new Destroyer();
        frigate.place(new Coordinate(0, 0), Orientation.RIGHT);
        destroyer.place(new Coordinate(1, 0), Orientation.RIGHT);
        Fleet fleet = new Fleet(List.of(frigate, destroyer));

        frigate.receiveHit(new Coordinate(0, 0));

        assertFalse(fleet.isCompletelySunk());
    }

    @Test
    void isCompletelySunkTrueOnlyWhenEveryShipIsSunk() {
        Frigate frigate = new Frigate();
        Destroyer destroyer = new Destroyer();
        frigate.place(new Coordinate(0, 0), Orientation.RIGHT);
        destroyer.place(new Coordinate(1, 0), Orientation.RIGHT);
        Fleet fleet = new Fleet(List.of(frigate, destroyer));

        frigate.receiveHit(new Coordinate(0, 0));
        destroyer.receiveHit(new Coordinate(1, 0));
        destroyer.receiveHit(new Coordinate(1, 1));

        assertTrue(fleet.isCompletelySunk());
    }

    @Test
    void countSunkShipsCountsOnlyTheSunkOnes() {
        Frigate sunkFrigate = new Frigate();
        Frigate aliveFrigate = new Frigate();
        sunkFrigate.place(new Coordinate(0, 0), Orientation.RIGHT);
        aliveFrigate.place(new Coordinate(1, 0), Orientation.RIGHT);
        Fleet fleet = new Fleet(List.of(sunkFrigate, aliveFrigate));

        sunkFrigate.receiveHit(new Coordinate(0, 0));

        assertEquals(1, fleet.countSunkShips());
    }
}
