package com.batallanaval.model;

import com.batallanaval.util.Coordenada;
import com.batallanaval.util.Orientacion;
import com.batallanaval.util.TipoBarco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representa un barco de la flota. Es abstracta: cada subclase concreta
 * (Portaaviones, Submarino, Destructor, Fragata) solo fija su TipoBarco;
 * toda la logica de impactos y hundimiento vive aqui.
 */
public abstract class Ship implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TipoBarco tipo;
    private List<Coordenada> posiciones;
    private Orientacion orientacion;
    private final Set<Coordenada> impactos = new HashSet<>();

    protected Ship(TipoBarco tipo) {
        this.tipo = tipo;
    }

    /**
     * Asigna las casillas que ocupa el barco en el tablero. Se llama una
     * sola vez durante la fase de colocacion; despues de esto el barco
     * queda fijo (no se puede mover ni modificar, segun HU-1).
     */
    public void colocar(Coordenada inicio, Orientacion orientacion) {
        this.orientacion = orientacion;
        this.posiciones = new ArrayList<>(tipo.getTamanio());
        for (int i = 0; i < tipo.getTamanio(); i++) {
            posiciones.add(inicio.desplazar(orientacion, i));
        }
    }

    /**
     * Registra que se recibio un disparo en la coordenada indicada.
     * Precondicion: la coordenada pertenece a este barco (ocupaCoordenada).
     */
    public void recibirImpacto(Coordenada coordenada) {
        impactos.add(coordenada);
    }

    public boolean ocupaCoordenada(Coordenada coordenada) {
        return posiciones != null && posiciones.contains(coordenada);
    }

    public boolean estaHundido() {
        return posiciones != null && impactos.size() >= posiciones.size();
    }

    public TipoBarco getTipo() {
        return tipo;
    }

    public List<Coordenada> getPosiciones() {
        return posiciones;
    }

    public Orientacion getOrientacion() {
        return orientacion;
    }

    public int getTamanio() {
        return tipo.getTamanio();
    }

    public String getNombre() {
        return tipo.name();
    }
}
