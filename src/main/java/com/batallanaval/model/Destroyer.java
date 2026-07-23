package com.batallanaval.model;

import com.batallanaval.util.ShipType;

/** Destroyer: occupies 2 cells. There are 3 per fleet. */
public class Destroyer extends Ship {
    private static final long serialVersionUID = 1L;

    public Destroyer() {
        super(ShipType.DESTROYER);
    }
}
