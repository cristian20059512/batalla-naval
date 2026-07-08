package com.batallanaval.model;

/**
 * Jugador humano. Su tablero propio es el "tablero de posicion": solo
 * observacion, refleja su flota y los disparos que ha recibido.
 */
public class HumanPlayer extends Player {

    private static final long serialVersionUID = 1L;

    public HumanPlayer(String nickname, Board tableroDePosicion) {
        super(nickname, tableroDePosicion);
    }
}
