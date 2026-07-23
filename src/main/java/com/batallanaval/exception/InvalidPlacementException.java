package com.batallanaval.exception;

/**
 * Checked exception: thrown when trying to place a ship outside the board
 * or overlapping another ship that is already placed.
 * It is checked because the caller (the view/controller) must ALWAYS decide
 * what to do about an invalid placement; it is not a programming error.
 */
public class InvalidPlacementException extends Exception {

    public InvalidPlacementException(String message) {
        super(message);
    }

    public InvalidPlacementException(String message, Throwable cause) {
        super(message, cause);
    }
}
