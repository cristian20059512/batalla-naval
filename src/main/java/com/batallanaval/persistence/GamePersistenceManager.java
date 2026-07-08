package com.batallanaval.persistence;

import com.batallanaval.exception.PersistenciaException;

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

    private static final Path CARPETA_GUARDADO = Paths.get("saves");

    private final PersistenceAdapter<EstadoPartida> adaptadorTablero =
            new SerializacionEstadoAdapter(CARPETA_GUARDADO.resolve("partida.dat"));
    private final PersistenceAdapter<ResumenPartida> adaptadorResumen =
            new ArchivoPlanoResumenAdapter(CARPETA_GUARDADO.resolve("resumen.txt"));

    public void guardarPartida(EstadoPartida estado, ResumenPartida resumen) throws PersistenciaException {
        adaptadorTablero.guardar(estado);
        adaptadorResumen.guardar(resumen);
    }

    public boolean existePartidaGuardada() {
        return adaptadorTablero.existeGuardado() && adaptadorResumen.existeGuardado();
    }

    public EstadoPartida cargarEstado() throws PersistenciaException {
        return adaptadorTablero.cargar();
    }

    public ResumenPartida cargarResumen() throws PersistenciaException {
        return adaptadorResumen.cargar();
    }

    public void eliminarPartidaGuardada() throws PersistenciaException {
        adaptadorTablero.eliminarGuardado();
        adaptadorResumen.eliminarGuardado();
    }
}
