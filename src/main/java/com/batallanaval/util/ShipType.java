package com.batallanaval.util;

import java.io.Serializable;

/**
 * Ship types defined by the assignment, with the size (number of cells it
 * occupies) and how many units of it each fleet has.
 */
public enum ShipType implements Serializable {
    AIRCRAFT_CARRIER(4, 1),
    SUBMARINE(3, 2),
    DESTROYER(2, 3),
    FRIGATE(1, 4);

    private final int size;
    private final int countInFleet;

    ShipType(int size, int countInFleet) {
        this.size = size;
        this.countInFleet = countInFleet;
    }

    public int getSize() {
        return size;
    }

    public int getCountInFleet() {
        return countInFleet;
    }
}
