package com.batallanaval.exception;

/**
 * Excepcion no marcada (unchecked): se lanza cuando se dispara sobre una
 * coordenada fuera del tablero o ya disparada antes (agua, tocado u hundido).
 * Es unchecked porque representa un error de logica del llamador (la UI no
 * deberia permitir seleccionar esa celda en primer lugar), no una condicion
 * de negocio que siempre haya que manejar explicitamente.
 */
public class InvalidShotException extends RuntimeException {

    public InvalidShotException(String message) {
        super(message);
    }
}
