package com.batallanaval.model;

import com.batallanaval.util.TipoBarco;

import java.util.ArrayList;
import java.util.List;

/**
 * Patron Factory Method: centraliza la creacion de instancias de {@link Ship}
 * segun el {@link TipoBarco}, evitando esparcir un switch/if-else de
 * instanciacion por todo el codigo de colocacion de flota.
 */
public final class ShipFactory {

    private ShipFactory() {
        // clase de utilidad, no se instancia
    }

    public static Ship crear(TipoBarco tipo) {
        switch (tipo) {
            case PORTAAVIONES:
                return new Portaaviones();
            case SUBMARINO:
                return new Submarino();
            case DESTRUCTOR:
                return new Destructor();
            case FRAGATA:
                return new Fragata();
            default:
                throw new IllegalArgumentException("Tipo de barco no soportado: " + tipo);
        }
    }

    /**
     * Crea la flota estandar completa (10 barcos, sin posicion asignada
     * todavia): 1 portaaviones, 2 submarinos, 3 destructores, 4 fragatas.
     */
    public static List<Ship> crearFlotaCompleta() {
        List<Ship> barcos = new ArrayList<>();
        for (TipoBarco tipo : TipoBarco.values()) {
            for (int i = 0; i < tipo.getCantidadEnFlota(); i++) {
                barcos.add(crear(tipo));
            }
        }
        return barcos;
    }
}
