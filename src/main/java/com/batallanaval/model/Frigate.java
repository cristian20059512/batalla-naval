package com.batallanaval.model;

import com.batallanaval.util.ShipType;

/** Fragata: ocupa 1 casilla. Hay 4 por flota. Se hunde con un solo disparo. */
public class Frigate extends Ship {
    private static final long serialVersionUID = 1L;

    public Frigate() {
        super(ShipType.FRIGATE);
    }
}
