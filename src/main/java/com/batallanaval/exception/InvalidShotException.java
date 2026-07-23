package com.batallanaval.exception;

/**
 * Unchecked exception: thrown when firing at a coordinate outside the board
 * or one that was already fired at before (water, hit, or sunk).
 * It is unchecked because it represents a logic error on the caller's side
 * (the UI should not allow that cell to be selected in the first place),
 * not a business condition that always has to be handled explicitly.
 */
public class InvalidShotException extends RuntimeException {

    public InvalidShotException(String message) {
        super(message);
    }
}
