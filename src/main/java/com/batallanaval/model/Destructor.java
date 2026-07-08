package com.batallanaval.model;

import com.batallanaval.util.TipoBarco;

/** Destructor: ocupa 2 casillas. Hay 3 por flota. */
public class Destructor extends Ship {
    private static final long serialVersionUID = 1L;

    public Destructor() {
        super(TipoBarco.DESTRUCTOR);
    }
}
