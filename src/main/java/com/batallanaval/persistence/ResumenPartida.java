package com.batallanaval.persistence;

/**
 * Resumen legible de la partida (nickname y conteo de barcos hundidos por
 * bando). No implementa {@code Serializable}: se guarda como texto plano
 * clave=valor a traves de {@link ArchivoPlanoResumenAdapter}, no con
 * serializacion binaria.
 */
public class ResumenPartida {

    private final String nicknameHumano;
    private final String nicknameMaquina;
    private final int barcosHundidosHumano;
    private final int totalBarcosHumano;
    private final int barcosHundidosMaquina;
    private final int totalBarcosMaquina;

    public ResumenPartida(String nicknameHumano, String nicknameMaquina, int barcosHundidosHumano,
                           int totalBarcosHumano, int barcosHundidosMaquina, int totalBarcosMaquina) {
        this.nicknameHumano = nicknameHumano;
        this.nicknameMaquina = nicknameMaquina;
        this.barcosHundidosHumano = barcosHundidosHumano;
        this.totalBarcosHumano = totalBarcosHumano;
        this.barcosHundidosMaquina = barcosHundidosMaquina;
        this.totalBarcosMaquina = totalBarcosMaquina;
    }

    public String getNicknameHumano() {
        return nicknameHumano;
    }

    public String getNicknameMaquina() {
        return nicknameMaquina;
    }

    public int getBarcosHundidosHumano() {
        return barcosHundidosHumano;
    }

    public int getTotalBarcosHumano() {
        return totalBarcosHumano;
    }

    public int getBarcosHundidosMaquina() {
        return barcosHundidosMaquina;
    }

    public int getTotalBarcosMaquina() {
        return totalBarcosMaquina;
    }
}
