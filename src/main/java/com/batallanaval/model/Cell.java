package com.batallanaval.model;

import com.batallanaval.util.Coordenada;
import com.batallanaval.util.EstadoCelda;

import java.io.Serializable;

/**
 * Una casilla del tablero. Guarda su posicion, su estado visible actual y
 * una referencia opcional al barco que ocupa (null si esta vacia o es agua).
 */
public class Cell implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Coordenada coordenada;
    private EstadoCelda estado;
    private Ship barco;

    public Cell(Coordenada coordenada) {
        this.coordenada = coordenada;
        this.estado = EstadoCelda.VACIA;
        this.barco = null;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }

    public EstadoCelda getEstado() {
        return estado;
    }

    public void setEstado(EstadoCelda estado) {
        this.estado = estado;
    }

    public Ship getBarco() {
        return barco;
    }

    public void setBarco(Ship barco) {
        this.barco = barco;
    }

    public boolean tieneBarco() {
        return barco != null;
    }

    public boolean yaFueDisparada() {
        return estado == EstadoCelda.AGUA || estado == EstadoCelda.TOCADO || estado == EstadoCelda.HUNDIDO;
    }
}
