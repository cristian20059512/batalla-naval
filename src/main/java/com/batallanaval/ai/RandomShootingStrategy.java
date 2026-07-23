package com.batallanaval.ai;

import com.batallanaval.model.Board;
import com.batallanaval.model.Cell;
import com.batallanaval.util.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Random shooting strategy: picks at random among the cells that have not
 * been fired at yet. Meets HU-4's minimum requirement ("The machine
 * selects shooting cells randomly").
 */
public class RandomShootingStrategy implements ShootingStrategy {

    private final Random random = new Random();

    @Override
    public Coordinate chooseShot(Board enemyBoard) {
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
