package com.batallanaval.ai;

import com.batallanaval.model.Board;
import com.batallanaval.model.Cell;
import com.batallanaval.util.Coordenada;

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
    public Coordenada elegirDisparo(Board tableroEnemigo) {
        List<Coordenada> disponibles = new ArrayList<>();
        for (int fila = 0; fila < Board.TAMANIO; fila++) {
            for (int columna = 0; columna < Board.TAMANIO; columna++) {
                Coordenada coordenada = new Coordenada(fila, columna);
                Cell celda = tableroEnemigo.getCelda(coordenada);
                if (!celda.yaFueDisparada()) {
                    disponibles.add(coordenada);
                }
            }
        }

        if (disponibles.isEmpty()) {
            throw new IllegalStateException("No quedan casillas disponibles para disparar.");
        }

        return disponibles.get(random.nextInt(disponibles.size()));
    }
}
