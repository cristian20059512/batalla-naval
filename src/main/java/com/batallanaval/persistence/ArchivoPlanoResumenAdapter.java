package com.batallanaval.persistence;

import com.batallanaval.exception.PersistenciaException;

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
 * {@link SerializacionEstadoAdapter} aunque el formato de almacenamiento es
 * completamente distinto (texto legible en vez de binario), que es la idea
 * del patron: el resto de la app no distingue entre ambos.
 */
public class ArchivoPlanoResumenAdapter implements PersistenceAdapter<ResumenPartida> {

    private final Path archivo;

    public ArchivoPlanoResumenAdapter(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public void guardar(ResumenPartida datos) throws PersistenciaException {
        try {
            Files.createDirectories(archivo.toAbsolutePath().getParent());
            List<String> lineas = List.of(
                    "nicknameHumano=" + datos.getNicknameHumano(),
                    "nicknameMaquina=" + datos.getNicknameMaquina(),
                    "barcosHundidosHumano=" + datos.getBarcosHundidosHumano(),
                    "totalBarcosHumano=" + datos.getTotalBarcosHumano(),
                    "barcosHundidosMaquina=" + datos.getBarcosHundidosMaquina(),
                    "totalBarcosMaquina=" + datos.getTotalBarcosMaquina());
            Files.write(archivo, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo guardar el resumen de la partida en " + archivo, e);
        }
    }

    @Override
    public ResumenPartida cargar() throws PersistenciaException {
        try {
            Map<String, String> valores = new HashMap<>();
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                int separador = linea.indexOf('=');
                if (separador > 0) {
                    valores.put(linea.substring(0, separador), linea.substring(separador + 1));
                }
            }
            return new ResumenPartida(
                    valores.getOrDefault("nicknameHumano", "Jugador"),
                    valores.getOrDefault("nicknameMaquina", "Maquina"),
                    Integer.parseInt(valores.getOrDefault("barcosHundidosHumano", "0")),
                    Integer.parseInt(valores.getOrDefault("totalBarcosHumano", "0")),
                    Integer.parseInt(valores.getOrDefault("barcosHundidosMaquina", "0")),
                    Integer.parseInt(valores.getOrDefault("totalBarcosMaquina", "0")));
        } catch (IOException | NumberFormatException e) {
            throw new PersistenciaException("No se pudo cargar el resumen de la partida desde " + archivo, e);
        }
    }

    @Override
    public boolean existeGuardado() {
        return Files.exists(archivo);
    }

    @Override
    public void eliminarGuardado() throws PersistenciaException {
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo eliminar el archivo de resumen " + archivo, e);
        }
    }
}
