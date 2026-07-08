package com.batallanaval.persistence;

import com.batallanaval.exception.PersistenciaException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Adaptador (patron Adapter) que expone el guardado del estado de los
 * tableros a traves de {@link PersistenceAdapter}, delegando en la API
 * nativa de serializacion de Java ({@link ObjectOutputStream} /
 * {@link ObjectInputStream}). El resto de la aplicacion solo conoce el
 * contrato {@code guardar/cargar}; el detalle de que esto es un archivo
 * binario serializado queda encapsulado aqui.
 */
public class SerializacionEstadoAdapter implements PersistenceAdapter<EstadoPartida> {

    private final Path archivo;

    public SerializacionEstadoAdapter(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public void guardar(EstadoPartida datos) throws PersistenciaException {
        try {
            Files.createDirectories(archivo.toAbsolutePath().getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(archivo)))) {
                oos.writeObject(datos);
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo guardar el estado del tablero en " + archivo, e);
        }
    }

    @Override
    public EstadoPartida cargar() throws PersistenciaException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {
            return (EstadoPartida) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException("No se pudo cargar el estado del tablero desde " + archivo, e);
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
            throw new PersistenciaException("No se pudo eliminar el archivo de guardado " + archivo, e);
        }
    }
}
