package com.batallanaval.persistence;

import com.batallanaval.model.Board;
import com.batallanaval.util.FaseJuego;
import com.batallanaval.util.Orientacion;
import com.batallanaval.util.TipoBarco;
import com.batallanaval.util.TurnoJuego;

import java.io.Serializable;
import java.util.List;

/**
 * Fotografia serializable de todo lo necesario para reanudar la partida:
 * los dos tableros completos (con flotas, celdas e impactos ya aplicados) y
 * el estado de turno/fase/colocacion del {@code GameController}. Es lo que
 * el adaptador de serializacion guarda en el archivo binario.
 */
public class EstadoPartida implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Board tableroPosicionHumano;
    private final Board tableroPrincipalMaquina;
    private final FaseJuego fase;
    private final TurnoJuego turno;
    private final List<TipoBarco> colaColocacion;
    private final Orientacion orientacionActual;
    private final boolean modoVerificacion;

    public EstadoPartida(Board tableroPosicionHumano, Board tableroPrincipalMaquina, FaseJuego fase,
                          TurnoJuego turno, List<TipoBarco> colaColocacion, Orientacion orientacionActual,
                          boolean modoVerificacion) {
        this.tableroPosicionHumano = tableroPosicionHumano;
        this.tableroPrincipalMaquina = tableroPrincipalMaquina;
        this.fase = fase;
        this.turno = turno;
        this.colaColocacion = colaColocacion;
        this.orientacionActual = orientacionActual;
        this.modoVerificacion = modoVerificacion;
    }

    public Board getTableroPosicionHumano() {
        return tableroPosicionHumano;
    }

    public Board getTableroPrincipalMaquina() {
        return tableroPrincipalMaquina;
    }

    public FaseJuego getFase() {
        return fase;
    }

    public TurnoJuego getTurno() {
        return turno;
    }

    public List<TipoBarco> getColaColocacion() {
        return colaColocacion;
    }

    public Orientacion getOrientacionActual() {
        return orientacionActual;
    }

    public boolean isModoVerificacion() {
        return modoVerificacion;
    }
}
