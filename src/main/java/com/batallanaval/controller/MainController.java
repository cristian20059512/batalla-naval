package com.batallanaval.controller;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador de la vista principal (FXML). Su unica responsabilidad es
 * conectar los componentes de la interfaz con el {@link GameController},
 * que contiene toda la logica real de la partida.
 */
public class MainController {

    private static final Logger LOG = Logger.getLogger(MainController.class.getName());

    /** Pseudo-clase CSS que marca la brujula como activa (modo verificacion encendido). */
    private static final PseudoClass VERIFYING = PseudoClass.getPseudoClass("verifying");

    @FXML
    private Pane positionBoardContainer;

    @FXML
    private Pane mainBoardContainer;

    @FXML
    private Label statusLabel;

    @FXML
    private Label timerLabel;

    @FXML
    private Button compassButton;

    @FXML
    private Button rotateButton;

    @FXML
    private Button randomFleetButton;

    private GameController gameController;

    @FXML
    public void initialize() {
        gameController = new GameController(positionBoardContainer, mainBoardContainer, statusLabel, timerLabel);
        compassButton.pseudoClassStateChanged(VERIFYING, gameController.isVerificationMode());

        // HU-3: el boton de verificacion no debe poder usarse como trampa
        // durante la partida; el controlador avisa cuando debe habilitarse
        // o deshabilitarse (antes de empezar, durante el juego y al terminar).
        gameController.setOnVerificationAvailabilityChanged(available -> compassButton.setDisable(!available));

        // Heuristica de usabilidad "prevencion de errores": una vez colocada
        // la flota estos botones ya no hacen nada, asi que se deshabilitan
        // en vez de dejarlos activos sin efecto.
        gameController.setOnPlacementAvailabilityChanged(available -> {
            rotateButton.setDisable(!available);
            randomFleetButton.setDisable(!available);
        });

        // el Scene todavia no existe en este punto (se asigna despues de cargar
        // el FXML), asi que el atajo de teclado se registra apenas este nodo
        // quede colgado de un Scene real.
        positionBoardContainer.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
            }
        });
    }

    /**
     * Atajos de teclado: R gira el barco actual, ESPACIO coloca la flota
     * aleatoria, V alterna la verificacion del tablero enemigo y ESCAPE
     * vuelve al menu principal (los mismos botones/acciones ya existentes,
     * solo que accesibles sin mouse). placeRandomFleet() y
     * toggleVerification() ya validan internamente si la accion aplica a
     * la fase actual, asi que es seguro invocarlos en cualquier momento.
     */
    private void onKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case R -> gameController.toggleOrientation();
            case SPACE -> gameController.placeRandomFleet();
            case V -> onToggleVerification();
            case ESCAPE -> onBackToMenu();
            default -> { }
        }
    }

    @FXML
    private void onRotateShip() {
        gameController.toggleOrientation();
    }

    @FXML
    private void onPlaceRandomFleet() {
        gameController.placeRandomFleet();
    }

    @FXML
    private void onToggleVerification() {
        boolean active = gameController.toggleVerification();
        compassButton.pseudoClassStateChanged(VERIFYING, active);
    }

    /**
     * "Salida de emergencia" hacia el menu principal (heuristica de
     * usabilidad "control y libertad del usuario"): la partida ya se guarda
     * sola tras cada jugada, asi que el jugador puede salir sin perder
     * progreso y retomarla despues con "Bitacora de viaje".
     */
    @FXML
    private void onBackToMenu() {
        gameController.shutdown();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/batallanaval/view/start-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            // start-view.fxml usa anclajes fijos pensados para el tamano con
            // el que Main.java la carga la primera vez (1280x690); si se
            // reabre con el tamano de la pantalla de juego (900x650) los
            // botones anclados quedan sin espacio y se ven rotos/incompletos.
            stage.setScene(new Scene(root, 1280, 690));
            stage.setResizable(false);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo volver al menu principal.", e);
            statusLabel.setText("No se pudo volver al menu: " + e.getMessage());
        }
    }
}
