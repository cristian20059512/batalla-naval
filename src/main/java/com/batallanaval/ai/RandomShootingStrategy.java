package com.batallanaval.ai;

import com.batallanaval.model.Board;
import com.batallanaval.model.Cell;
import com.batallanaval.util.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Estrategia de disparo aleatoria: elige al azar entre las casillas que
 * todavia no han sido disparadas. Cumple el requisito minimo de HU-4
 * ("La maquina selecciona casillas de disparo de manera aleatoria").
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
