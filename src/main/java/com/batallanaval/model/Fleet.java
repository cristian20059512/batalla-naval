package com.batallanaval.model;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a player's whole fleet (10 ships). It knows nothing about the
 * board; it only groups the ships and answers aggregate status questions,
 * such as how many are still afloat.
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
