package com.batallanaval.model;

import com.batallanaval.util.ShipType;

/** Portaaviones: ocupa 4 casillas. Hay 1 por flota. */
public class AircraftCarrier extends Ship {
    private static final long serialVersionUID = 1L;

    public AircraftCarrier() {
        super(ShipType.AIRCRAFT_CARRIER);
    }
}
