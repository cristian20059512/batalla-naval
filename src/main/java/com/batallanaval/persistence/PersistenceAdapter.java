package com.batallanaval.persistence;

import com.batallanaval.exception.PersistenceException;

/**
 * Puerto comun (patron Adapter) que el resto de la aplicacion usa para
 * guardar y cargar datos, sin conocer el mecanismo de E/S real detras.
 * Cada implementacion "adapta" una tecnologia de persistencia distinta
 * (serializacion binaria, archivo de texto plano, etc.) a esta misma
 * interfaz, de modo que quien la consume (GamePersistenceManager) puede
 * tratarlas de forma intercambiable.
 */
public interface PersistenceAdapter<T> {

    void save(T data) throws PersistenceException;

    T load() throws PersistenceException;

    boolean hasSavedData();

    void deleteSavedData() throws PersistenceException;
}
