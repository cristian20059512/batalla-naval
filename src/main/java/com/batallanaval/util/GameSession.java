package com.batallanaval.util;

/**
 * Holds session data that needs to be passed from the start screen
 * (StartController) to the game board (GameController) without coupling
 * the two controllers to each other.
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
