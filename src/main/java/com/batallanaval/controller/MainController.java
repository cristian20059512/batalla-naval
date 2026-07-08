package com.batallanaval.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * Controlador de la vista principal (FXML). Su unica responsabilidad es
 * conectar los componentes de la interfaz con el {@link GameController},
 * que contiene toda la logica real de la partida.
 */
public class MainController {

    @FXML
    private Pane positionBoardContainer;

    @FXML
    private Pane mainBoardContainer;

    @FXML
    private Label statusLabel;

    private GameController gameController;

    @FXML
    public void initialize() {
        gameController = new GameController(positionBoardContainer, mainBoardContainer, statusLabel);
    }

    @FXML
    private void onGirarBarco() {
        gameController.alternarOrientacion();
    }

    @FXML
    private void onColocarAleatorio() {
        gameController.colocarFlotaAleatoria();
    }

    @FXML
    private void onVerTableroEnemigo() {
        gameController.alternarVerificacion();
    }
}
