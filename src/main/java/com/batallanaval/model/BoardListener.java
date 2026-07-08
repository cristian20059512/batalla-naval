package com.batallanaval.model;

import com.batallanaval.util.Coordinate;
import com.batallanaval.util.CellState;

/**
 * Patron Observer: quien implemente esta interfaz (tipicamente un
 * controlador de vista JavaFX) sera notificado cada vez que una celda del
 * {@link Board} cambia de estado, sin que el modelo conozca nada de JavaFX.
 * Esto es lo que permite "programacion orientada a eventos" desacoplada
 * de la logica del juego.
 */
public interface BoardListener {

    void onCellChanged(Coordinate coordinate, CellState newState);
}
