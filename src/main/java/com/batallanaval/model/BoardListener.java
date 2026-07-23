package com.batallanaval.model;

import com.batallanaval.util.Coordinate;
import com.batallanaval.util.CellState;

/**
 * Observer pattern: whoever implements this interface (typically a JavaFX
 * view controller) is notified every time a {@link Board} cell changes
 * state, without the model knowing anything about JavaFX. This is what
 * enables "event-oriented programming" decoupled from the game logic.
 */
public interface BoardListener {

    void onCellChanged(Coordinate coordinate, CellState newState);
}
