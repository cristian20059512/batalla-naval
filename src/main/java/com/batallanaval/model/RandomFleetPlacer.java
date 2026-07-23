package com.batallanaval.model;

import com.batallanaval.exception.InvalidPlacementException;
import com.batallanaval.util.Coordinate;
import com.batallanaval.util.Orientation;

import java.util.List;
import java.util.Random;

/**
 * Places a complete fleet of 10 ships on a board at random, respecting
 * HU-1's rules (no overlap, within the board). Used to automatically
 * generate the machine's main board (HU-4), and also works as a shortcut
 * if the human wants "random placement" on their own placement board.
 */
public final class RandomFleetPlacer {

    private static final int MAX_ATTEMPTS_PER_SHIP = 500;

    private RandomFleetPlacer() {
        // clase de utilidad, no se instancia
    }

    public static Fleet placeRandomFleet(Board board) {
        List<Ship> ships = ShipFactory.createFullFleet();
        placeShipsRandomly(board, ships);
        Fleet fleet = new Fleet(ships);
        board.setFleet(fleet);
        return fleet;
    }

    /**
     * Places each ship in the given list at random, without overlapping or
     * going outside the board. Unlike {@link #placeRandomFleet(Board)}, it
     * does not create a fleet from scratch nor replace the board's
     * {@code Fleet}: it only fills in the ships still missing when part of
     * the fleet was already placed by hand (otherwise, "Place random fleet"
     * would end up adding 10 new ships on top of the ones already placed).
     */
    public static void placeShipsRandomly(Board board, List<Ship> ships) {
        Random random = new Random();

        for (Ship ship : ships) {
            boolean placed = false;
            for (int attempt = 0; attempt < MAX_ATTEMPTS_PER_SHIP && !placed; attempt++) {
                Orientation[] orientations = Orientation.values();
                Orientation orientation = orientations[random.nextInt(orientations.length)];
                Coordinate start = new Coordinate(
                        random.nextInt(Board.SIZE),
                        random.nextInt(Board.SIZE));
                try {
                    board.placeShip(ship, start, orientation);
                    placed = true;
                } catch (InvalidPlacementException e) {
                    // coordenada invalida o superpuesta: se reintenta con otra al azar
                }
            }
            if (!placed) {
                throw new IllegalStateException(
                        "No se pudo colocar el barco " + ship.getName() + " tras "
                                + MAX_ATTEMPTS_PER_SHIP + " intentos.");
            }
        }
    }
}
