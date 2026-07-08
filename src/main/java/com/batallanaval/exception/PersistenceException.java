package com.batallanaval.exception;

/**
 * Excepcion marcada (checked): envuelve cualquier fallo de lectura/escritura
 * al guardar o cargar el estado de la partida (archivos serializables o
 * archivos planos). Checked porque el fallo de IO siempre debe ser
 * capturado y comunicado al usuario, nunca ignorado.
 */
public class PersistenceException extends Exception {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(String message) {
        super(message);
    }
}
