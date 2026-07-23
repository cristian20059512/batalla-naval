package com.batallanaval.model;

import com.batallanaval.util.ShipType;

/** Submarine: occupies 3 cells. There are 2 per fleet. */
public class Submarine extends Ship {
    private static final long serialVersionUID = 1L;

    public Submarine() {
        super(ShipType.SUBMARINE);
    }
}
