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
    private final Board ownBoard;
    private final List<Shot> shotHistory = new ArrayList<>();

    protected Player(String nickname, Board ownBoard) {
        this.nickname = nickname;
        this.ownBoard = ownBoard;
    }

    public String getNickname() {
        return nickname;
    }

    public Board getOwnBoard() {
        return ownBoard;
    }

    public List<Shot> getShotHistory() {
        return shotHistory;
    }

    public void recordShot(Shot shot) {
        shotHistory.add(shot);
    }

    public int countOwnSunkShips() {
        return ownBoard.getFleet() == null
                ? 0
                : (int) ownBoard.getFleet().countSunkShips();
    }
}
