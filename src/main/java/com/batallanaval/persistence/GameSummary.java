package com.batallanaval.persistence;

/**
 * Resumen legible de la partida (nickname y conteo de barcos hundidos por
 * bando). No implementa {@code Serializable}: se guarda como texto plano
 * clave=valor a traves de {@link PlainTextSummaryAdapter}, no con
 * serializacion binaria.
 */
public class GameSummary {

    private final String humanNickname;
    private final String machineNickname;
    private final int humanSunkShips;
    private final int humanTotalShips;
    private final int machineSunkShips;
    private final int machineTotalShips;

    public GameSummary(String humanNickname, String machineNickname, int humanSunkShips,
                        int humanTotalShips, int machineSunkShips, int machineTotalShips) {
        this.humanNickname = humanNickname;
        this.machineNickname = machineNickname;
        this.humanSunkShips = humanSunkShips;
        this.humanTotalShips = humanTotalShips;
        this.machineSunkShips = machineSunkShips;
        this.machineTotalShips = machineTotalShips;
    }

    public String getHumanNickname() {
        return humanNickname;
    }

    public String getMachineNickname() {
        return machineNickname;
    }

    public int getHumanSunkShips() {
        return humanSunkShips;
    }

    public int getHumanTotalShips() {
        return humanTotalShips;
    }

    public int getMachineSunkShips() {
        return machineSunkShips;
    }

    public int getMachineTotalShips() {
        return machineTotalShips;
    }
}
