package com.batallanaval.ai;

import com.batallanaval.model.Board;
import com.batallanaval.model.Cell;
import com.batallanaval.util.CellState;
import com.batallanaval.util.Coordinate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Estrategia "cazar y rematar": si hay alguna casilla en estado TOCADO
 * (barco dañado pero no hundido todavia), dispara al azar entre las
 * casillas vecinas (arriba/abajo/izquierda/derecha) que aun no se hayan
 * disparado, para intentar hundir ese barco antes de seguir explorando.
 * Si no hay ningun TOCADO pendiente, dispara al azar sobre todo el
 * tablero, igual que {@link RandomShootingStrategy}.
 *
 * No necesita que le avisen el resultado del disparo anterior: en cada
 * turno vuelve a inspeccionar el tablero, y las casillas TOCADO son
 * exactamente los impactos que todavia no formaron parte de un barco
 * hundido (ver {@link com.batallanaval.model.Board#shoot}).
 */
public class HuntTargetShootingStrategy implements ShootingStrategy {

    private final Random random = new Random();

    @Override
    public Coordinate chooseShot(Board enemyBoard) {
        List<Coordinate> huntCandidates = findCandidatesAroundHits(enemyBoard);
        if (!huntCandidates.isEmpty()) {
            return huntCandidates.get(random.nextInt(huntCandidates.size()));
        }
        return chooseRandomCell(enemyBoard);
    }

    private List<Coordinate> findCandidatesAroundHits(Board board) {
        Set<Coordinate> candidates = new LinkedHashSet<>();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                Coordinate coordinate = new Coordinate(row, column);
                if (board.getCell(coordinate).getState() == CellState.HIT) {
                    addIfShootable(board, candidates, new Coordinate(row - 1, column));
                    addIfShootable(board, candidates, new Coordinate(row + 1, column));
                    addIfShootable(board, candidates, new Coordinate(row, column - 1));
                    addIfShootable(board, candidates, new Coordinate(row, column + 1));
                }
            }
        }
        return new ArrayList<>(candidates);
    }

    private void addIfShootable(Board board, Set<Coordinate> candidates, Coordinate coordinate) {
        if (coordinate.isWithinBoard(Board.SIZE) && !board.getCell(coordinate).wasAlreadyShot()) {
            candidates.add(coordinate);
        }
    }

    private Coordinate chooseRandomCell(Board enemyBoard) {
        List<Coordinate> available = new ArrayList<>();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                Coordinate coordinate = new Coordinate(row, column);
                Cell cell = enemyBoard.getCell(coordinate);
                if (!cell.wasAlreadyShot()) {
                    available.add(coordinate);
                }
            }
        }

        if (available.isEmpty()) {
            throw new IllegalStateException("No quedan casillas disponibles para disparar.");
        }

        return available.get(random.nextInt(available.size()));
    }
}
