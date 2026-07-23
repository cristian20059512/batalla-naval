package com.batallanaval.persistence;

import com.batallanaval.exception.PersistenceException;

/**
 * Common port (Adapter pattern) the rest of the application uses to save
 * and load data, without knowing the actual I/O mechanism behind it. Each
 * implementation "adapts" a different persistence technology (binary
 * serialization, plain text file, etc.) to this same interface, so that
 * whoever consumes it (GamePersistenceManager) can treat them
 * interchangeably.
 */
public interface PersistenceAdapter<T> {

    void save(T data) throws PersistenceException;

    T load() throws PersistenceException;

    boolean hasSavedData();

    void deleteSavedData() throws PersistenceException;
}
