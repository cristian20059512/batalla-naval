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
 * Adaptador (patron Adapter) que expone el guardado del resumen de la
 * partida (nickname y barcos hundidos) a traves de {@link PersistenceAdapter},
 * delegando en lectura/escritura de texto plano linea por linea
 * ({@code clave=valor}). Cumple exactamente el mismo contrato que
 * {@link SerializedStateAdapter} aunque el formato de almacenamiento es
 * completamente distinto (texto legible en vez de binario), que es la idea
 * del patron: el resto de la app no distingue entre ambos.
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
