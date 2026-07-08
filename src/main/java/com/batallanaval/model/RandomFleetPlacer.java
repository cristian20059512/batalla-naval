package com.batallanaval.model;

import com.batallanaval.exception.ColocacionInvalidaException;
import com.batallanaval.util.Coordenada;
import com.batallanaval.util.Orientacion;

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

    private static final int INTENTOS_MAXIMOS_POR_BARCO = 500;

    private RandomFleetPlacer() {
        // clase de utilidad, no se instancia
    }

    public static Fleet colocarFlotaAleatoria(Board tablero) {
        List<Ship> barcos = ShipFactory.crearFlotaCompleta();
        Random random = new Random();

        for (Ship barco : barcos) {
            boolean colocado = false;
            for (int intento = 0; intento < INTENTOS_MAXIMOS_POR_BARCO && !colocado; intento++) {
                Orientacion orientacion = random.nextBoolean() ? Orientacion.HORIZONTAL : Orientacion.VERTICAL;
                Coordenada inicio = new Coordenada(
                        random.nextInt(Board.TAMANIO),
                        random.nextInt(Board.TAMANIO));
                try {
                    tablero.colocarBarco(barco, inicio, orientacion);
                    colocado = true;
                } catch (ColocacionInvalidaException e) {
                    // coordenada invalida o superpuesta: se reintenta con otra al azar
                }
            }
            if (!colocado) {
                throw new IllegalStateException(
                        "No se pudo colocar el barco " + barco.getNombre() + " tras "
                                + INTENTOS_MAXIMOS_POR_BARCO + " intentos.");
            }
        }

        Fleet flota = new Fleet(barcos);
        tablero.setFlota(flota);
        return flota;
    }
}
