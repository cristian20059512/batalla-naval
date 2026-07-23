package com.batallanaval.persistence;

import com.batallanaval.exception.PersistenceException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Single access point for game persistence. Coordinates the two adapters
 * (serialized board + plain text summary) so {@code GameController} can
 * save/load the whole game with a single call, without knowing that there
 * are two files with two different I/O mechanisms behind them (that is
 * each {@link PersistenceAdapter}'s responsibility).
 */
public class GamePersistenceManager {

    private static final Path SAVE_FOLDER = Paths.get("saves");

    private final PersistenceAdapter<GameState> stateAdapter =
            new SerializedStateAdapter(SAVE_FOLDER.resolve("partida.dat"));
    private final PersistenceAdapter<GameSummary> summaryAdapter =
            new PlainTextSummaryAdapter(SAVE_FOLDER.resolve("resumen.txt"));

    public void saveGame(GameState state, GameSummary summary) throws PersistenceException {
        stateAdapter.save(state);
        summaryAdapter.save(summary);
    }

    public boolean hasSavedGame() {
        return stateAdapter.hasSavedData() && summaryAdapter.hasSavedData();
    }

    public GameState loadState() throws PersistenceException {
        return stateAdapter.load();
    }

    public GameSummary loadSummary() throws PersistenceException {
        return summaryAdapter.load();
    }

    public void deleteSavedGame() throws PersistenceException {
        stateAdapter.deleteSavedData();
        summaryAdapter.deleteSavedData();
    }
}
