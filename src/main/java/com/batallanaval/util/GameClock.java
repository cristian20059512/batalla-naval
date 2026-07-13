package com.batallanaval.util;

import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * Hilo independiente que funciona como cronometro de la partida: cada
 * segundo actualiza (via {@link Platform#runLater}) un {@link Label} con el
 * tiempo transcurrido desde que empezo el turno de disparos.
 *
 * El arranque y la detencion estan sincronizados sobre {@code this} para que
 * nunca queden dos hilos de cronometro corriendo a la vez, aunque
 * {@link #start()}/{@link #stop()} se invoquen desde distintos puntos del
 * ciclo de vida de la partida (nueva partida, partida cargada, fin de
 * partida).
 */
public class GameClock {

    private final Label timerLabel;
    private Thread thread;
    private volatile boolean running = false;
    private int elapsedSeconds = 0;

    public GameClock(Label timerLabel) {
        this.timerLabel = timerLabel;
    }

    /** Arranca el cronometro desde cero. Si ya estaba corriendo, no hace nada. */
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        elapsedSeconds = 0;
        thread = new Thread(this::tick, "game-clock");
        thread.setDaemon(true);
        thread.start();
    }

    /** Detiene el cronometro (fin de partida o cierre de la vista). */
    public synchronized void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    private void tick() {
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            elapsedSeconds++;
            String text = String.format("Tiempo: %02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60);
            Platform.runLater(() -> {
                if (running) {
                    timerLabel.setText(text);
                }
            });
        }
    }
}
