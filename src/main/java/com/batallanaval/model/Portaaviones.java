package com.batallanaval.model;

import com.batallanaval.util.TipoBarco;

/** Portaaviones: ocupa 4 casillas. Hay 1 por flota. */
public class Portaaviones extends Ship {
    private static final long serialVersionUID = 1L;

    public Portaaviones() {
        super(TipoBarco.PORTAAVIONES);
    }
}
