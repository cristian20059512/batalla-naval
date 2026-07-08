package com.batallanaval.exception;

/**
 * Excepcion marcada (checked): se lanza cuando se intenta colocar un barco
 * fuera del tablero o superpuesto con otro barco ya colocado.
 * Es checked porque el llamador (la vista/controlador) SIEMPRE debe decidir
 * que hacer ante una colocacion invalida, no es un error de programacion.
 */
public class InvalidPlacementException extends Exception {

    public InvalidPlacementException(String message) {
        super(message);
    }

    public InvalidPlacementException(String message, Throwable cause) {
        super(message, cause);
    }
}
