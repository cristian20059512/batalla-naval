package com.batallanaval.persistence;

import com.batallanaval.exception.PersistenceException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter (Adapter pattern) that exposes saving the game summary (nickname
 * and sunk ships) through {@link PersistenceAdapter}, delegating to
 * line-by-line plain text reading/writing ({@code key=value}). It follows
 * exactly the same contract as {@link SerializedStateAdapter} even though
 * the storage format is completely different (readable text instead of
 * binary), which is the whole point of the pattern: the rest of the app
 * cannot tell the two apart.
 */
public class PlainTextSummaryAdapter implements PersistenceAdapter<GameSummary> {

    private final Path file;

    public PlainTextSummaryAdapter(Path file) {
        this.file = file;
    }

    @Override
    public void save(GameSummary data) throws PersistenceException {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            List<String> lines = List.of(
                    "humanNickname=" + data.getHumanNickname(),
                    "machineNickname=" + data.getMachineNickname(),
                    "humanSunkShips=" + data.getHumanSunkShips(),
                    "humanTotalShips=" + data.getHumanTotalShips(),
                    "machineSunkShips=" + data.getMachineSunkShips(),
                    "machineTotalShips=" + data.getMachineTotalShips());
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("No se pudo guardar el resumen de la partida en " + file, e);
        }
    }

    @Override
    public GameSummary load() throws PersistenceException {
        try {
            Map<String, String> values = new HashMap<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                int separator = line.indexOf('=');
                if (separator > 0) {
                    values.put(line.substring(0, separator), line.substring(separator + 1));
                }
            }
            return new GameSummary(
                    values.getOrDefault("humanNickname", "Jugador"),
                    values.getOrDefault("machineNickname", "Maquina"),
                    Integer.parseInt(values.getOrDefault("humanSunkShips", "0")),
                    Integer.parseInt(values.getOrDefault("humanTotalShips", "0")),
                    Integer.parseInt(values.getOrDefault("machineSunkShips", "0")),
                    Integer.parseInt(values.getOrDefault("machineTotalShips", "0")));
        } catch (IOException | NumberFormatException e) {
            throw new PersistenceException("No se pudo cargar el resumen de la partida desde " + file, e);
        }
    }

    @Override
    public boolean hasSavedData() {
        return Files.exists(file);
    }

    @Override
    public void deleteSavedData() throws PersistenceException {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new PersistenceException("No se pudo eliminar el archivo de resumen " + file, e);
        }
    }
}
