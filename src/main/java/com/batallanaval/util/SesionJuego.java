package com.batallanaval.util;

/**
 * Guarda datos de sesion que se necesitan pasar de la pantalla inicial
 * (StartController) al tablero de juego (GameController) sin acoplar
 * los dos controladores entre si.
 */
public final class SesionJuego {

    private static String nicknameHumano = "Jugador";

    private SesionJuego() {
    }

    public static void setNicknameHumano(String nickname) {
        nicknameHumano = (nickname == null || nickname.isBlank()) ? "Jugador" : nickname.trim();
    }

    public static String getNicknameHumano() {
        return nicknameHumano;
    }
}
