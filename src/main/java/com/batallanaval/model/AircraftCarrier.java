package com.batallanaval.model;

import com.batallanaval.util.ShipType;

/** Aircraft carrier: occupies 4 cells. There is 1 per fleet. */
public class AircraftCarrier extends Ship {
    private static final long serialVersionUID = 1L;

    public AircraftCarrier() {
        super(ShipType.AIRCRAFT_CARRIER);
    }
}
