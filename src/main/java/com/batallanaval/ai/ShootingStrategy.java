package com.batallanaval.ai;

import com.batallanaval.model.Board;
import com.batallanaval.util.Coordenada;

/**
 * Patron Strategy: define como la maquina elige su proxima coordenada de
 * disparo. Permite cambiar el comportamiento de la IA (por ejemplo, de
 * aleatorio puro a uno que "cace" alrededor de un impacto reciente) sin
 * tocar {@link com.batallanaval.model.MachinePlayer} ni el controlador.
 */
public interface ShootingStrategy {

    /**
     * Elige la siguiente coordenada de disparo sobre el tablero del
     * oponente, garantizando que no sea una casilla ya disparada.
     */
    Coordenada elegirDisparo(Board tableroEnemigo);
}
