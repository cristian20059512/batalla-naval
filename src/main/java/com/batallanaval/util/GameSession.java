package com.batallanaval.util;

/**
 * Guarda datos de sesion que se necesitan pasar de la pantalla inicial
 * (StartController) al tablero de juego (GameController) sin acoplar
 * los dos controladores entre si.
 */
public final class GameSession {

    private static String humanNickname = "Jugador";

    private GameSession() {
    }

    public static void setHumanNickname(String nickname) {
        humanNickname = (nickname == null || nickname.isBlank()) ? "Jugador" : nickname.trim();
    }

    public static String getHumanNickname() {
        return humanNickname;
    }
}
