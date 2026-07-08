package com.batallanaval.persistence;

import com.batallanaval.exception.PersistenceException;

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
 * contrato {@code save/load}; el detalle de que esto es un archivo
 * binario serializado queda encapsulado aqui.
 */
public class SerializedStateAdapter implements PersistenceAdapter<GameState> {

    private final Path file;

    public SerializedStateAdapter(Path file) {
        this.file = file;
    }

    @Override
    public void save(GameState data) throws PersistenceException {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(file)))) {
                oos.writeObject(data);
            }
        } catch (IOException e) {
            throw new PersistenceException("No se pudo guardar el estado del tablero en " + file, e);
        }
    }

    @Override
    public GameState load() throws PersistenceException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            return (GameState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenceException("No se pudo cargar el estado del tablero desde " + file, e);
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
            throw new PersistenceException("No se pudo eliminar el archivo de guardado " + file, e);
        }
    }
}
