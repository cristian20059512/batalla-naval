package com.batallanaval.model;

import com.batallanaval.util.ShipType;

/** Destructor: ocupa 2 casillas. Hay 3 por flota. */
public class Destroyer extends Ship {
    private static final long serialVersionUID = 1L;

    public Destroyer() {
        super(ShipType.DESTROYER);
    }
}
