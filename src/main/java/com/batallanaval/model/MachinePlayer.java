package com.batallanaval.model;

import com.batallanaval.ai.ShootingStrategy;
import com.batallanaval.util.Coordinate;

/**
 * Jugador maquina. Su tablero propio es el "tablero principal" del
 * oponente: se genera automaticamente (flota colocada al azar) y es donde
 * el jugador humano dispara. Delega la eleccion de sus propios disparos
 * (contra el tablero del humano) en una {@link ShootingStrategy}, lo que
 * permite cambiar de comportamiento sin tocar esta clase (patron Strategy).
 */
public class MachinePlayer extends Player {

    private static final long serialVersionUID = 1L;

    private final ShootingStrategy shootingStrategy;

    public MachinePlayer(String nickname, Board mainBoard, ShootingStrategy shootingStrategy) {
        super(nickname, mainBoard);
        this.shootingStrategy = shootingStrategy;
    }

    /**
     * Elige la coordenada donde la maquina va a disparar dentro del
     * tablero del jugador humano, usando la estrategia configurada.
     */
    public Coordinate chooseTarget(Board humanBoard) {
        return shootingStrategy.chooseShot(humanBoard);
    }
}
