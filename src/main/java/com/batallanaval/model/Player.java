package com.batallanaval.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Jugador de la partida (humano o maquina). Guarda su propio tablero (donde
 * tiene su flota colocada, ya sea el tablero de posicion del humano o el
 * tablero principal de la maquina) y el historial de disparos que ha hecho
 * sobre el tablero del oponente.
 */
public abstract class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nickname;
    private final Board tableroPropio;
    private final List<Shot> historialDisparos = new ArrayList<>();

    protected Player(String nickname, Board tableroPropio) {
        this.nickname = nickname;
        this.tableroPropio = tableroPropio;
    }

    public String getNickname() {
        return nickname;
    }

    public Board getTableroPropio() {
        return tableroPropio;
    }

    public List<Shot> getHistorialDisparos() {
        return historialDisparos;
    }

    public void registrarDisparo(Shot disparo) {
        historialDisparos.add(disparo);
    }

    public int contarBarcosHundidosPropios() {
        return tableroPropio.getFlota() == null
                ? 0
                : (int) tableroPropio.getFlota().contarBarcosHundidos();
    }
}
