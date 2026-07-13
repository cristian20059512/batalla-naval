package com.batallanaval.model;

import com.batallanaval.exception.InvalidPlacementException;
import com.batallanaval.util.Coordinate;
import com.batallanaval.util.Orientation;

import java.util.List;
import java.util.Random;

/**
 * Coloca una flota completa de 10 barcos en un tablero de forma aleatoria,
 * respetando las reglas de HU-1 (sin superposicion, dentro del tablero).
 * Se usa para generar automaticamente el tablero principal de la maquina
 * (HU-4), y tambien sirve como atajo si el humano quiere "colocacion
 * aleatoria" en su propio tablero de posicion.
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
     * Coloca al azar, sin superposicion ni salirse del tablero, cada barco
     * de la lista dada. A diferencia de {@link #placeRandomFleet(Board)},
     * no crea una flota desde cero ni reemplaza el {@code Fleet} del
     * tablero: sirve para completar solo los barcos que todavia falten
     * cuando parte de la flota ya se coloco a mano (si no, "Colocar flota
     * aleatoria" terminaria agregando 10 barcos nuevos encima de los que ya
     * estaban puestos).
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
