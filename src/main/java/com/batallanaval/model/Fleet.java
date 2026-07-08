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

    private final List<Ship> barcos;

    public Fleet(List<Ship> barcos) {
        this.barcos = barcos;
    }

    public List<Ship> getBarcos() {
        return barcos;
    }

    public boolean estaCompletamenteHundida() {
        return barcos.stream().allMatch(Ship::estaHundido);
    }

    public long contarBarcosHundidos() {
        return barcos.stream().filter(Ship::estaHundido).count();
    }

    public int getTotalBarcos() {
        return barcos.size();
    }
}
