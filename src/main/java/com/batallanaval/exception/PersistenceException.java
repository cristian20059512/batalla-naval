package com.batallanaval.exception;

/**
 * Checked exception: wraps any read/write failure when saving or loading the
 * game state (serializable files or plain text files). It is checked because
 * an IO failure must always be caught and reported to the user, never ignored.
 */
public class PersistenceException extends Exception {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(String message) {
        super(message);
    }
}
