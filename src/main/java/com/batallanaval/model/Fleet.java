package com.batallanaval.model;

import java.io.Serializable;
import java.util.List;

/**
 * Representa la flota completa de un jugador (10 barcos). No conoce nada
 * del tablero; solo agrupa los barcos y responde preguntas de estado
 * agregado, como cuantos siguen a flote.
 */
public class Fleet implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Ship> ships;

    public Fleet(List<Ship> ships) {
        this.ships = ships;
    }

    public List<Ship> getShips() {
        return ships;
    }

    public boolean isCompletelySunk() {
        return ships.stream().allMatch(Ship::isSunk);
    }

    public long countSunkShips() {
        return ships.stream().filter(Ship::isSunk).count();
    }

    public int getTotalShips() {
        return ships.size();
    }
}
