package com.batallanaval.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoordinateTest {

    @Test
    void shiftRightIncreasesColumn() {
        Coordinate start = new Coordinate(3, 3);
        Coordinate shifted = start.shift(Orientation.RIGHT, 2);
        assertEquals(3, shifted.getRow());
        assertEquals(5, shifted.getColumn());
    }

    @Test
    void shiftLeftDecreasesColumn() {
        Coordinate start = new Coordinate(3, 3);
        Coordinate shifted = start.shift(Orientation.LEFT, 2);
        assertEquals(3, shifted.getRow());
        assertEquals(1, shifted.getColumn());
    }

    @Test
    void shiftDownIncreasesRow() {
        Coordinate start = new Coordinate(3, 3);
        Coordinate shifted = start.shift(Orientation.DOWN, 2);
        assertEquals(5, shifted.getRow());
        assertEquals(3, shifted.getColumn());
    }

    @Test
    void shiftUpDecreasesRow() {
        Coordinate start = new Coordinate(3, 3);
        Coordinate shifted = start.shift(Orientation.UP, 2);
        assertEquals(1, shifted.getRow());
        assertEquals(3, shifted.getColumn());
    }

    @Test
    void isWithinBoardTrueForValidCoordinates() {
        assertTrue(new Coordinate(0, 0).isWithinBoard(10));
        assertTrue(new Coordinate(9, 9).isWithinBoard(10));
        assertTrue(new Coordinate(5, 5).isWithinBoard(10));
    }

    @Test
    void isWithinBoardFalseForOutOfRangeCoordinates() {
        assertFalse(new Coordinate(-1, 0).isWithinBoard(10));
        assertFalse(new Coordinate(0, -1).isWithinBoard(10));
        assertFalse(new Coordinate(10, 0).isWithinBoard(10));
        assertFalse(new Coordinate(0, 10).isWithinBoard(10));
    }

    @Test
    void equalsAndHashCodeMatchForSameRowAndColumn() {
        Coordinate a = new Coordinate(4, 7);
        Coordinate b = new Coordinate(4, 7);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsFalseForDifferentCoordinates() {
        Coordinate a = new Coordinate(4, 7);
        Coordinate b = new Coordinate(7, 4);
        assertNotEquals(a, b);
    }
}
