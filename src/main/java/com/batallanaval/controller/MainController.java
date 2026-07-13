package com.batallanaval.controller;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

/**
 * Controlador de la vista principal (FXML). Su unica responsabilidad es
 * conectar los componentes de la interfaz con el {@link GameController},
 * que contiene toda la logica real de la partida.
 */
public class MainController {

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

    private GameController gameController;

    @FXML
    public void initialize() {
        gameController = new GameController(positionBoardContainer, mainBoardContainer, statusLabel, timerLabel);
        compassButton.pseudoClassStateChanged(VERIFYING, gameController.isVerificationMode());

        // HU-3: el boton de verificacion no debe poder usarse como trampa
        // durante la partida; el controlador avisa cuando debe habilitarse
        // o deshabilitarse (antes de empezar, durante el juego y al terminar).
        gameController.setOnVerificationAvailabilityChanged(available -> compassButton.setDisable(!available));

        // el Scene todavia no existe en este punto (se asigna despues de cargar
        // el FXML), asi que el atajo de teclado se registra apenas este nodo
        // quede colgado de un Scene real.
        positionBoardContainer.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
            }
        });
    }

    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.R) {
            gameController.toggleOrientation();
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
}
