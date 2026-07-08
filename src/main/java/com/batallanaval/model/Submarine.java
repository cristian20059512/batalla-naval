package com.batallanaval.model;

import com.batallanaval.util.ShipType;

/** Submarino: ocupa 3 casillas. Hay 2 por flota. */
public class Submarine extends Ship {
    private static final long serialVersionUID = 1L;

    public Submarine() {
        super(ShipType.SUBMARINE);
    }
}
