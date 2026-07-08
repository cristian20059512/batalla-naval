package com.batallanaval.model;

import com.batallanaval.util.Coordenada;
import com.batallanaval.util.ResultadoDisparo;

import java.io.Serializable;

/**
 * Registro inmutable de un disparo ya realizado: donde y con que resultado.
 * Se usa para el historial de la partida y para que la IA de la maquina
 * sepa que casillas ya intento.
 */
public class Shot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Coordenada coordenada;
    private final ResultadoDisparo resultado;

    public Shot(Coordenada coordenada, ResultadoDisparo resultado) {
        this.coordenada = coordenada;
        this.resultado = resultado;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }

    public ResultadoDisparo getResultado() {
        return resultado;
    }
}
