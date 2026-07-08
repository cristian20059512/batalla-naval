package com.batallanaval.model;

import com.batallanaval.util.Coordinate;
import com.batallanaval.util.ShotResult;

import java.io.Serializable;

/**
 * Registro inmutable de un disparo ya realizado: donde y con que resultado.
 * Se usa para el historial de la partida y para que la IA de la maquina
 * sepa que casillas ya intento.
 */
public class Shot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Coordinate coordinate;
    private final ShotResult result;

    public Shot(Coordinate coordinate, ShotResult result) {
        this.coordinate = coordinate;
        this.result = result;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public ShotResult getResult() {
        return result;
    }
}
