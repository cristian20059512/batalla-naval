package com.batallanaval.model;

import com.batallanaval.exception.ColocacionInvalidaException;
import com.batallanaval.exception.DisparoInvalidoException;
import com.batallanaval.util.Coordenada;
import com.batallanaval.util.EstadoCelda;
import com.batallanaval.util.Orientacion;
import com.batallanaval.util.ResultadoDisparo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Tablero de 10x10 casillas. Contiene la logica de negocio de colocacion de
 * barcos (HU-1) y de disparos (HU-2/HU-4): validaciones, deteccion de
 * agua/tocado/hundido, y notificacion a la vista via el patron Observer
 * (ver {@link BoardListener}).
 *
 * La misma clase sirve tanto para el tablero de posicion del jugador humano
 * como para el tablero principal (propio de la maquina o del humano segun
 * quien dispare), ya que ambos son, en esencia, un grid 10x10 con una flota.
 */
public class Board implements Serializable {

    public static final int TAMANIO = 10;

    private static final long serialVersionUID = 1L;

    private final Cell[][] celdas;
    private Fleet flota;

    // transient: los listeners (controladores JavaFX) no se serializan;
    // se vuelven a registrar al recargar una partida guardada.
    private final transient List<BoardListener> listeners = new ArrayList<>();

    public Board() {
        celdas = new Cell[TAMANIO][TAMANIO];
        for (int fila = 0; fila < TAMANIO; fila++) {
            for (int columna = 0; columna < TAMANIO; columna++) {
                celdas[fila][columna] = new Cell(new Coordenada(fila, columna));
            }
        }
    }

    public void setFlota(Fleet flota) {
        this.flota = flota;
    }

    public Fleet getFlota() {
        return flota;
    }

    public Cell getCelda(Coordenada coordenada) {
        return celdas[coordenada.getFila()][coordenada.getColumna()];
    }

    public void agregarListener(BoardListener listener) {
        listeners.add(listener);
    }

    public void removerListener(BoardListener listener) {
        listeners.remove(listener);
    }

    private void notificar(Coordenada coordenada, EstadoCelda estado) {
        for (BoardListener listener : listeners) {
            listener.onCambioCelda(coordenada, estado);
        }
    }

    /**
     * Coloca un barco en el tablero validando las reglas de HU-1: dentro
     * del tablero y sin superposicion con otro barco ya colocado.
     * Una vez colocado, el barco no puede volver a colocarse (no se provee
     * metodo de "mover"), cumpliendo la definicion de hecho de HU-1.
     */
    public void colocarBarco(Ship barco, Coordenada inicio, Orientacion orientacion)
            throws ColocacionInvalidaException {

        List<Coordenada> posiciones = calcularPosiciones(barco, inicio, orientacion);

        for (Coordenada coordenada : posiciones) {
            if (!coordenada.dentroDelTablero(TAMANIO)) {
                throw new ColocacionInvalidaException(
                        "El barco " + barco.getNombre() + " queda fuera del tablero en " + coordenada);
            }
            if (getCelda(coordenada).tieneBarco()) {
                throw new ColocacionInvalidaException(
                        "El barco " + barco.getNombre() + " se superpone con otro barco en " + coordenada);
            }
        }

        barco.colocar(inicio, orientacion);
        for (Coordenada coordenada : posiciones) {
            Cell celda = getCelda(coordenada);
            celda.setBarco(barco);
            celda.setEstado(EstadoCelda.BARCO);
        }
    }

    private List<Coordenada> calcularPosiciones(Ship barco, Coordenada inicio, Orientacion orientacion) {
        List<Coordenada> posiciones = new ArrayList<>(barco.getTamanio());
        for (int i = 0; i < barco.getTamanio(); i++) {
            posiciones.add(inicio.desplazar(orientacion, i));
        }
        return posiciones;
    }

    /**
     * Procesa un disparo sobre la coordenada indicada (HU-2/HU-4).
     * Devuelve el resultado (AGUA, TOCADO o HUNDIDO) y notifica a los
     * listeners registrados para que la vista se actualice en tiempo real.
     */
    public ResultadoDisparo disparar(Coordenada coordenada) {
        if (!coordenada.dentroDelTablero(TAMANIO)) {
            throw new DisparoInvalidoException("La coordenada " + coordenada + " esta fuera del tablero.");
        }

        Cell celda = getCelda(coordenada);
        if (celda.yaFueDisparada()) {
            throw new DisparoInvalidoException("Ya se disparo antes en " + coordenada + ".");
        }

        ResultadoDisparo resultado;
        if (!celda.tieneBarco()) {
            celda.setEstado(EstadoCelda.AGUA);
            resultado = ResultadoDisparo.AGUA;
        } else {
            Ship barco = celda.getBarco();
            barco.recibirImpacto(coordenada);
            if (barco.estaHundido()) {
                celda.setEstado(EstadoCelda.HUNDIDO);
                resultado = ResultadoDisparo.HUNDIDO;
                marcarBarcoHundido(barco);
            } else {
                celda.setEstado(EstadoCelda.TOCADO);
                resultado = ResultadoDisparo.TOCADO;
            }
        }

        notificar(coordenada, celda.getEstado());
        return resultado;
    }

    /**
     * Cuando un barco se hunde, todas sus casillas (incluidas las que ya
     * estaban en TOCADO) pasan a mostrarse como HUNDIDO, tal como pide el
     * enunciado ("aparecera en el tablero el barco completo con la marca
     * indicativa de que ha sido hundido").
     */
    private void marcarBarcoHundido(Ship barco) {
        for (Coordenada coordenada : barco.getPosiciones()) {
            Cell celda = getCelda(coordenada);
            celda.setEstado(EstadoCelda.HUNDIDO);
            notificar(coordenada, EstadoCelda.HUNDIDO);
        }
    }

    public boolean todaLaFlotaHundida() {
        return flota != null && flota.estaCompletamenteHundida();
    }

    /**
     * Copia el estado (celdas y flota) de un tablero recien deserializado
     * dentro de este tablero ya construido, en vez de reemplazar la
     * instancia. Asi los listeners ya registrados (la vista JavaFX) y la
     * referencia final a este {@code Board} siguen siendo validos despues
     * de cargar una partida guardada.
     */
    public void restaurarEstado(Board origen) {
        for (int fila = 0; fila < TAMANIO; fila++) {
            for (int columna = 0; columna < TAMANIO; columna++) {
                Coordenada coordenada = new Coordenada(fila, columna);
                Cell celdaOrigen = origen.getCelda(coordenada);
                Cell celdaDestino = getCelda(coordenada);
                celdaDestino.setBarco(celdaOrigen.getBarco());
                celdaDestino.setEstado(celdaOrigen.getEstado());
            }
        }
        this.flota = origen.getFlota();
    }
}
