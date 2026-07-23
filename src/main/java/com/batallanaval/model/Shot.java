package com.batallanaval.model;

import com.batallanaval.util.Coordinate;
import com.batallanaval.util.ShotResult;

import java.io.Serializable;

/**
 * Immutable record of a shot already taken: where, and with what result.
 * Used for the game's history and so the machine's AI knows which cells
 * it has already tried.
 */
public class Shot implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Coordinate coordinate;
    private final ShotResult result;

    public Shot(Coordinate coordinate, ShotResult result) {
        this.coordinate = coordinate;
        this.result = result;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public ShotResult getResult() {
        return result;
    }
}
