package com.batallanaval.model;

import com.batallanaval.util.ShipType;

import java.util.ArrayList;
import java.util.List;

/**
 * Patron Factory Method: centraliza la creacion de instancias de {@link Ship}
 * segun el {@link ShipType}, evitando esparcir un switch/if-else de
 * instanciacion por todo el codigo de colocacion de flota.
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
     * Crea la flota estandar completa (10 barcos, sin posicion asignada
     * todavia): 1 portaaviones, 2 submarinos, 3 destructores, 4 fragatas.
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
