package com.batallanaval.util;

import java.io.Serializable;

/**
 * Tipos de barco definidos por el enunciado, con el tamano (numero de
 * casillas que ocupa) y la cantidad de unidades que tiene cada flota.
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
