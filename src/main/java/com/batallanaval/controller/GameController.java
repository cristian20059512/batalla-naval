package com.batallanaval.controller;

import com.batallanaval.ai.RandomShootingStrategy;
import com.batallanaval.exception.ColocacionInvalidaException;
import com.batallanaval.exception.DisparoInvalidoException;
import com.batallanaval.exception.PersistenciaException;
import com.batallanaval.model.Board;
import com.batallanaval.model.HumanPlayer;
import com.batallanaval.model.MachinePlayer;
import com.batallanaval.model.RandomFleetPlacer;
import com.batallanaval.model.Ship;
import com.batallanaval.model.ShipFactory;
import com.batallanaval.model.Shot;
import com.batallanaval.persistence.EstadoPartida;
import com.batallanaval.persistence.GamePersistenceManager;
import com.batallanaval.persistence.ResumenPartida;
import com.batallanaval.util.Coordenada;
import com.batallanaval.util.FaseJuego;
import com.batallanaval.util.Orientacion;
import com.batallanaval.util.ResultadoDisparo;
import com.batallanaval.util.TipoBarco;
import com.batallanaval.util.TurnoJuego;
import com.batallanaval.view.BoardView;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orquesta la partida completa: fase de colocacion (HU-1), turnos de
 * disparo humano/maquina (HU-2/HU-4), verificacion del tablero enemigo
 * (HU-3) y deteccion de victoria. Es el "Controlador" de la arquitectura
 * MVC: conoce el Modelo (Board, Player) y la Vista (BoardView), pero la
 * Vista y el Modelo no se conocen entre si directamente (salvo por el
 * Observer BoardListener, que ya desacopla esa relacion).
 */
public class GameController {

    private static final Logger LOG = Logger.getLogger(GameController.class.getName());

    private final Board tableroPosicionHumano = new Board();
    private final Board tableroPrincipalMaquina = new Board();

    private final BoardView vistaPosicion;
    private final BoardView vistaPrincipal;
    private final Label statusLabel;

    private final GamePersistenceManager persistencia = new GamePersistenceManager();

    private HumanPlayer humano;
    private MachinePlayer maquina;

    private FaseJuego fase = FaseJuego.COLOCACION;
    private TurnoJuego turno = TurnoJuego.HUMANO;

    private final List<TipoBarco> colaColocacion = new ArrayList<>();
    private Orientacion orientacionActual = Orientacion.HORIZONTAL;
    private boolean modoVerificacion = false;

    public GameController(Pane contenedorPosicion, Pane contenedorPrincipal, Label statusLabel) {
        this.statusLabel = statusLabel;

        vistaPosicion = new BoardView(tableroPosicionHumano, true);
        vistaPrincipal = new BoardView(tableroPrincipalMaquina, false);

        contenedorPosicion.getChildren().add(vistaPosicion.getNodo());
        contenedorPrincipal.getChildren().add(vistaPrincipal.getNodo());

        tableroPosicionHumano.agregarListener(vistaPosicion);
        tableroPrincipalMaquina.agregarListener(vistaPrincipal);

        vistaPosicion.setOnCeldaClic(this::onClickTableroPosicion);
        vistaPrincipal.setOnCeldaClic(this::onClickTableroPrincipal);

        if (persistencia.existePartidaGuardada()) {
            cargarPartidaGuardada();
        } else {
            prepararColaDeColocacion();
            actualizarStatusColocacion();
        }
    }

    /**
     * Reconstruye la partida a partir de los archivos de guardado (HU de
     * persistencia): copia el estado de los tableros deserializados dentro
     * de los tableros ya construidos (para no invalidar los listeners de
     * la vista) y retoma fase/turno/cola de colocacion tal como quedaron.
     */
    private void cargarPartidaGuardada() {
        try {
            EstadoPartida estado = persistencia.cargarEstado();
            ResumenPartida resumen = persistencia.cargarResumen();

            tableroPosicionHumano.restaurarEstado(estado.getTableroPosicionHumano());
            tableroPrincipalMaquina.restaurarEstado(estado.getTableroPrincipalMaquina());

            fase = estado.getFase();
            turno = estado.getTurno();
            colaColocacion.clear();
            colaColocacion.addAll(estado.getColaColocacion());
            orientacionActual = estado.getOrientacionActual();
            modoVerificacion = estado.isModoVerificacion();

            vistaPosicion.redibujarTodo();
            vistaPrincipal.setModoVerificacion(modoVerificacion);

            if (fase == FaseJuego.COLOCACION) {
                actualizarStatusColocacion();
                return;
            }

            humano = new HumanPlayer(resumen.getNicknameHumano(), tableroPosicionHumano);
            maquina = new MachinePlayer(resumen.getNicknameMaquina(), tableroPrincipalMaquina,
                    new RandomShootingStrategy());

            if (fase == FaseJuego.FIN) {
                statusLabel.setText("Partida cargada: la partida ya habia finalizado.");
            } else {
                statusLabel.setText("Partida cargada. Turno: " + turno);
                if (turno == TurnoJuego.MAQUINA) {
                    pausarYLuego(this::turnoMaquina);
                }
            }
        } catch (PersistenciaException e) {
            LOG.log(Level.WARNING, "No se pudo cargar la partida guardada, se inicia una nueva.", e);
            statusLabel.setText("No se pudo cargar la partida guardada, se inicia una nueva.");
            prepararColaDeColocacion();
            actualizarStatusColocacion();
        }
    }

    /**
     * Guardado automatico (HU de persistencia): guarda el estado completo
     * de los tableros (archivo serializable) y un resumen legible con
     * nickname y barcos hundidos (archivo plano) cada vez que la partida
     * cambia de estado. Un fallo de E/S no debe interrumpir el juego, solo
     * se registra en el log.
     */
    private void autoguardar() {
        EstadoPartida estado = new EstadoPartida(tableroPosicionHumano, tableroPrincipalMaquina, fase, turno,
                colaColocacion, orientacionActual, modoVerificacion);
        ResumenPartida resumen = new ResumenPartida(
                humano != null ? humano.getNickname() : "Jugador",
                maquina != null ? maquina.getNickname() : "Maquina",
                tableroPosicionHumano.getFlota() != null
                        ? (int) tableroPosicionHumano.getFlota().contarBarcosHundidos() : 0,
                tableroPosicionHumano.getFlota() != null ? tableroPosicionHumano.getFlota().getTotalBarcos() : 0,
                tableroPrincipalMaquina.getFlota() != null
                        ? (int) tableroPrincipalMaquina.getFlota().contarBarcosHundidos() : 0,
                tableroPrincipalMaquina.getFlota() != null ? tableroPrincipalMaquina.getFlota().getTotalBarcos() : 0);
        try {
            persistencia.guardarPartida(estado, resumen);
        } catch (PersistenciaException e) {
            LOG.log(Level.WARNING, "No se pudo guardar la partida automaticamente.", e);
        }
    }

    private void prepararColaDeColocacion() {
        for (TipoBarco tipo : TipoBarco.values()) {
            for (int i = 0; i < tipo.getCantidadEnFlota(); i++) {
                colaColocacion.add(tipo);
            }
        }
    }

    /** Invocado por el boton "Girar barco": alterna horizontal/vertical antes de colocar. */
    public void alternarOrientacion() {
        orientacionActual = orientacionActual == Orientacion.HORIZONTAL
                ? Orientacion.VERTICAL
                : Orientacion.HORIZONTAL;
        if (fase == FaseJuego.COLOCACION) {
            actualizarStatusColocacion();
        }
    }

    /** Invocado por el boton "Ver tablero enemigo": modo de verificacion (HU-3). */
    public void alternarVerificacion() {
        modoVerificacion = !modoVerificacion;
        vistaPrincipal.setModoVerificacion(modoVerificacion);
    }

    /** Invocado por el boton "Colocar flota aleatoria": atajo para no colocar barco por barco. */
    public void colocarFlotaAleatoria() {
        if (fase != FaseJuego.COLOCACION) {
            return;
        }
        colaColocacion.clear();
        RandomFleetPlacer.colocarFlotaAleatoria(tableroPosicionHumano);
        vistaPosicion.redibujarTodo();
        comenzarFaseDeJuego();
    }

    private void onClickTableroPosicion(Coordenada coordenada) {
        if (fase != FaseJuego.COLOCACION || colaColocacion.isEmpty()) {
            return;
        }
        TipoBarco tipo = colaColocacion.get(0);
        Ship barco = ShipFactory.crear(tipo);
        try {
            tableroPosicionHumano.colocarBarco(barco, coordenada, orientacionActual);
            colaColocacion.remove(0);
            if (colaColocacion.isEmpty()) {
                comenzarFaseDeJuego();
            } else {
                actualizarStatusColocacion();
            }
            autoguardar();
        } catch (ColocacionInvalidaException e) {
            statusLabel.setText("Colocacion invalida: " + e.getMessage());
        }
    }

    private void actualizarStatusColocacion() {
        if (colaColocacion.isEmpty()) {
            return;
        }
        TipoBarco siguiente = colaColocacion.get(0);
        statusLabel.setText("Coloca tu " + siguiente + " (" + siguiente.getTamanio()
                + " casillas) - Orientacion actual: " + orientacionActual);
    }

    private void comenzarFaseDeJuego() {
        fase = FaseJuego.JUEGO;
        humano = new HumanPlayer("Jugador", tableroPosicionHumano);

        RandomFleetPlacer.colocarFlotaAleatoria(tableroPrincipalMaquina);
        maquina = new MachinePlayer("Maquina", tableroPrincipalMaquina, new RandomShootingStrategy());
        vistaPrincipal.redibujarTodo();

        turno = TurnoJuego.HUMANO;
        statusLabel.setText("Flota lista. Es tu turno: dispara en el tablero enemigo.");
        autoguardar();
    }

    private void onClickTableroPrincipal(Coordenada coordenada) {
        if (fase != FaseJuego.JUEGO || turno != TurnoJuego.HUMANO) {
            return;
        }
        try {
            ResultadoDisparo resultado = tableroPrincipalMaquina.disparar(coordenada);
            humano.registrarDisparo(new Shot(coordenada, resultado));
            statusLabel.setText("Disparaste en " + coordenada + ": " + resultado);

            if (tableroPrincipalMaquina.todaLaFlotaHundida()) {
                finalizarJuego("Hundiste toda la flota enemiga. Ganaste.");
                return;
            }

            if (resultado == ResultadoDisparo.AGUA) {
                turno = TurnoJuego.MAQUINA;
                pausarYLuego(this::turnoMaquina);
            }
            // si fue TOCADO o HUNDIDO (y no gano todavia), el humano dispara de nuevo
            autoguardar();
        } catch (DisparoInvalidoException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    private void turnoMaquina() {
        Coordenada objetivo = maquina.elegirObjetivo(tableroPosicionHumano);
        ResultadoDisparo resultado = tableroPosicionHumano.disparar(objetivo);
        maquina.registrarDisparo(new Shot(objetivo, resultado));
        statusLabel.setText("La maquina disparo en " + objetivo + ": " + resultado);

        if (tableroPosicionHumano.todaLaFlotaHundida()) {
            finalizarJuego("La maquina hundio toda tu flota. Perdiste.");
            return;
        }

        if (resultado == ResultadoDisparo.AGUA) {
            turno = TurnoJuego.HUMANO;
            autoguardar();
        } else {
            autoguardar();
            pausarYLuego(this::turnoMaquina);
        }
    }

    /** Pequena pausa entre disparos de la maquina para que se alcancen a ver en la UI. */
    private void pausarYLuego(Runnable accion) {
        PauseTransition pausa = new PauseTransition(Duration.seconds(0.6));
        pausa.setOnFinished(evento -> accion.run());
        pausa.play();
    }

    private void finalizarJuego(String mensaje) {
        fase = FaseJuego.FIN;
        statusLabel.setText(mensaje);
        autoguardar();
    }
}
