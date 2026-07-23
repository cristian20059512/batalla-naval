package com.batallanaval.persistence;

import com.batallanaval.exception.PersistenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlainTextSummaryAdapterTest {

    @Test
    void hasSavedDataIsFalseBeforeAnySave(@TempDir Path tempDir) {
        PlainTextSummaryAdapter adapter = new PlainTextSummaryAdapter(tempDir.resolve("resumen.txt"));
        assertFalse(adapter.hasSavedData());
    }

    @Test
    void savingAndLoadingRoundTripsAllFields(@TempDir Path tempDir) throws PersistenceException {
        PlainTextSummaryAdapter adapter = new PlainTextSummaryAdapter(tempDir.resolve("resumen.txt"));
        GameSummary original = new GameSummary("Cristian", "Maquina", 3, 10, 5, 10);

        adapter.save(original);
        assertTrue(adapter.hasSavedData());

        GameSummary loaded = adapter.load();
        assertEquals("Cristian", loaded.getHumanNickname());
        assertEquals("Maquina", loaded.getMachineNickname());
        assertEquals(3, loaded.getHumanSunkShips());
        assertEquals(10, loaded.getHumanTotalShips());
        assertEquals(5, loaded.getMachineSunkShips());
        assertEquals(10, loaded.getMachineTotalShips());
    }

    @Test
    void deleteSavedDataRemovesTheFile(@TempDir Path tempDir) throws PersistenceException {
        PlainTextSummaryAdapter adapter = new PlainTextSummaryAdapter(tempDir.resolve("resumen.txt"));
        adapter.save(new GameSummary("Cristian", "Maquina", 0, 10, 0, 10));

        adapter.deleteSavedData();

        assertFalse(adapter.hasSavedData());
    }

    @Test
    void loadingMissingValuesFallsBackToDefaults(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("resumen.txt");
        java.nio.file.Files.writeString(file, "humanNickname=Solo\n");

        PlainTextSummaryAdapter adapter = new PlainTextSummaryAdapter(file);
        GameSummary loaded = adapter.load();

        assertEquals("Solo", loaded.getHumanNickname());
        assertEquals("Maquina", loaded.getMachineNickname());
        assertEquals(0, loaded.getHumanSunkShips());
    }
}
