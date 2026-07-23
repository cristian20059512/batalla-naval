package com.batallanaval.model;

import com.batallanaval.util.ShipType;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory Method pattern: centralizes creating {@link Ship} instances based
 * on {@link ShipType}, avoiding an instantiation switch/if-else scattered
 * throughout the fleet placement code.
 */
public final class ShipFactory {

    private ShipFactory() {
        // clase de utilidad, no se instancia
    }

    public static Ship create(ShipType type) {
        switch (type) {
            case AIRCRAFT_CARRIER:
                return new AircraftCarrier();
            case SUBMARINE:
                return new Submarine();
            case DESTROYER:
                return new Destroyer();
            case FRIGATE:
                return new Frigate();
            default:
                throw new IllegalArgumentException("Tipo de barco no soportado: " + type);
        }
    }

    /**
     * Creates the complete standard fleet (10 ships, with no position
     * assigned yet): 1 aircraft carrier, 2 submarines, 3 destroyers,
     * 4 frigates.
     */
    public static List<Ship> createFullFleet() {
        List<Ship> ships = new ArrayList<>();
        for (ShipType type : ShipType.values()) {
            for (int i = 0; i < type.getCountInFleet(); i++) {
                ships.add(create(type));
            }
        }
        return ships;
    }
}
