package com.batallanaval.util;

import java.io.Serializable;

/**
 * Direccion en la que un barco se extiende desde la casilla donde el
 * jugador hace clic (esa casilla es siempre la proa). Las 4 direcciones
 * cardinales permiten colocar el barco apuntando hacia cualquiera de los
 * 4 lados, sin introducir diagonales (que el enunciado no contempla).
 */
public enum Orientation implements Serializable {
    RIGHT,
    DOWN,
    LEFT,
    UP
}
