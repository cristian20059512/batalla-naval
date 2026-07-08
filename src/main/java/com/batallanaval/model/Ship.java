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
 * Representa un barco de la flota. Es abstracta: cada subclase concreta
 * (AircraftCarrier, Submarine, Destroyer, Frigate) solo fija su ShipType;
 * toda la logica de impactos y hundimiento vive aqui.
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
     * Asigna las casillas que ocupa el barco en el tablero. Se llama una
     * sola vez durante la fase de colocacion; despues de esto el barco
     * queda fijo (no se puede mover ni modificar, segun HU-1).
     */
    public void place(Coordinate start, Orientation orientation) {
        this.orientation = orientation;
        this.positions = new ArrayList<>(type.getSize());
        for (int i = 0; i < type.getSize(); i++) {
            positions.add(start.shift(orientation, i));
        }
    }

    /**
     * Registra que se recibio un disparo en la coordenada indicada.
     * Precondicion: la coordenada pertenece a este barco (occupiesCoordinate).
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
