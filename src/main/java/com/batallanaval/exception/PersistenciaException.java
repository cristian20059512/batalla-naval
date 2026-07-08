package com.batallanaval.exception;

/**
 * Excepcion marcada (checked): envuelve cualquier fallo de lectura/escritura
 * al guardar o cargar el estado de la partida (archivos serializables o
 * archivos planos). Checked porque el fallo de IO siempre debe ser
 * capturado y comunicado al usuario, nunca ignorado.
 */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }
}
