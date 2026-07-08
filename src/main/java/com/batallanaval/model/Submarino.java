package com.batallanaval.model;

import com.batallanaval.util.TipoBarco;

/** Submarino: ocupa 3 casillas. Hay 2 por flota. */
public class Submarino extends Ship {
    private static final long serialVersionUID = 1L;

    public Submarino() {
        super(TipoBarco.SUBMARINO);
    }
}
