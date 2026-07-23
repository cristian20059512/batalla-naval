package com.batallanaval.util;

import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * Independent thread that works as the game's stopwatch: every second it
 * updates (via {@link Platform#runLater}) a {@link Label} with the time
 * elapsed since the shooting turns started.
 *
 * Starting and stopping are synchronized on {@code this} so that two clock
 * threads are never left running at the same time, even if
 * {@link #start()}/{@link #stop()} are called from different points in the
 * game's lifecycle (new game, loaded game, game over).
 */
public class GameClock {

    private final Label timerLabel;
    private Thread thread;
    private volatile boolean running = false;
    private int elapsedSeconds = 0;

    public GameClock(Label timerLabel) {
        this.timerLabel = timerLabel;
    }

    /** Starts the stopwatch from zero. Does nothing if it was already running. */
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

    /** Stops the stopwatch (game over or view closed). */
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
