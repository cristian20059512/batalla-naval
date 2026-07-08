package com.batallanaval.model;

import com.batallanaval.util.TipoBarco;

/** Fragata: ocupa 1 casilla. Hay 4 por flota. Se hunde con un solo disparo. */
public class Fragata extends Ship {
    private static final long serialVersionUID = 1L;

    public Fragata() {
        super(TipoBarco.FRAGATA);
    }
}
