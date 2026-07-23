package com.batallanaval.model;

import com.batallanaval.util.Coordinate;
import com.batallanaval.util.Orientation;
import com.batallanaval.util.ShipType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a ship in the fleet. It is abstract: each concrete subclass
 * (AircraftCarrier, Submarine, Destroyer, Frigate) only sets its ShipType;
 * all the hit and sinking logic lives here.
 */
public abstract class Ship implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ShipType type;
    private List<Coordinate> positions;
    private Orientation orientation;
    private final Set<Coordinate> hits = new HashSet<>();

    protected Ship(ShipType type) {
        this.type = type;
    }

    /**
     * Assigns the cells the ship occupies on the board. Called only once
     * during the placement phase; after this the ship stays fixed (it
     * cannot be moved or modified, per HU-1).
     */
    public void place(Coordinate start, Orientation orientation) {
        this.orientation = orientation;
        this.positions = new ArrayList<>(type.getSize());
        for (int i = 0; i < type.getSize(); i++) {
            positions.add(start.shift(orientation, i));
        }
    }

    /**
     * Records that a shot was received at the given coordinate.
     * Precondition: the coordinate belongs to this ship (occupiesCoordinate).
     */
    public void receiveHit(Coordinate coordinate) {
        hits.add(coordinate);
    }

    public boolean occupiesCoordinate(Coordinate coordinate) {
        return positions != null && positions.contains(coordinate);
    }

    public boolean isSunk() {
        return positions != null && hits.size() >= positions.size();
    }

    public ShipType getType() {
        return type;
    }

    public List<Coordinate> getPositions() {
        return positions;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public int getSize() {
        return type.getSize();
    }

    public String getName() {
        return type.name();
    }
}
