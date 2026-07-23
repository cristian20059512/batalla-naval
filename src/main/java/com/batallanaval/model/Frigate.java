package com.batallanaval.model;

import com.batallanaval.util.ShipType;

/** Frigate: occupies 1 cell. There are 4 per fleet. Sinks with a single shot. */
public class Frigate extends Ship {
    private static final long serialVersionUID = 1L;

    public Frigate() {
        super(ShipType.FRIGATE);
    }
}
