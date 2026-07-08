package com.batallanaval.persistence;

import com.batallanaval.exception.PersistenceException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Punto unico de acceso a la persistencia de la partida. Coordina los dos
 * adaptadores (tablero serializado + resumen en texto plano) para que
 * {@code GameController} guarde/cargue la partida completa con una sola
 * llamada, sin conocer que son dos archivos con dos mecanismos de E/S
 * distintos detras (eso es responsabilidad de cada {@link PersistenceAdapter}).
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
