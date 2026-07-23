package com.batallanaval.persistence;

/**
 * Human-readable game summary (nickname and count of sunk ships per side).
 * Does not implement {@code Serializable}: it is saved as plain text
 * key=value pairs via {@link PlainTextSummaryAdapter}, not through binary
 * serialization.
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
