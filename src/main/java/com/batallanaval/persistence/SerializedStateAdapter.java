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
 * Adapter (Adapter pattern) that exposes saving the boards' state through
 * {@link PersistenceAdapter}, delegating to Java's native serialization API
 * ({@link ObjectOutputStream} / {@link ObjectInputStream}). The rest of the
 * application only knows the {@code save/load} contract; the detail that
 * this is a serialized binary file stays encapsulated here.
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
