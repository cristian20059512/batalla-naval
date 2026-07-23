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
 * "Hunt and finish" strategy: if there is any cell in the HIT state (a
 * damaged ship that isn't sunk yet), it fires at random among the
 * neighboring cells (up/down/left/right) that haven't been fired at yet,
 * to try to sink that ship before exploring further. If there is no
 * pending HIT, it fires at random over the whole board, just like
 * {@link RandomShootingStrategy}.
 *
 * It doesn't need to be told the previous shot's result: on every turn it
 * inspects the board again, and the HIT cells are exactly the hits that
 * are not yet part of a sunk ship (see {@link com.batallanaval.model.Board#shoot}).
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
