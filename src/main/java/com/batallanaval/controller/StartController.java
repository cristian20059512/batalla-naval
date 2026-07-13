package com.batallanaval.controller;

import com.batallanaval.exception.PersistenceException;
import com.batallanaval.persistence.GamePersistenceManager;
import com.batallanaval.util.GameSession;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador de la pantalla inicial (FXML): pide el nickname del jugador
 * y ofrece "Jugar" (partida nueva) o "Continuar" (retoma la ultima partida
 * guardada, ver {@link GamePersistenceManager}).
 */
public class StartController {

    private static final Logger LOG = Logger.getLogger(StartController.class.getName());

    @FXML
    private TextField nicknameField;

    @FXML
    private Button continueButton;

    @FXML
    private Label messageLabel;

    private final GamePersistenceManager persistenceManager = new GamePersistenceManager();

    @FXML
    public void initialize() {
        continueButton.setDisable(!persistenceManager.hasSavedGame());
    }

    @FXML
    private void onPlay() {
        GameSession.setHumanNickname(nicknameField.getText());
        try {
            persistenceManager.deleteSavedGame();
        } catch (PersistenceException e) {
            LOG.log(Level.WARNING, "No se pudo borrar la partida guardada anterior.", e);
        }
        goToGame();
    }

    @FXML
    private void onContinue() {
        goToGame();
    }

    /**
     * Muestra las instrucciones del juego (heuristica de usabilidad "ayuda
     * y documentacion"): reglas basicas, terminologia y atajos de teclado,
     * para que el jugador no tenga que adivinarlos por prueba y error.
     */
    @FXML
    private void onOptions() {
        messageLabel.setTextFill(Color.web("#F5E6C8"));
        messageLabel.setText(
                "Coloca tu flota (1 portaaviones, 2 submarinos, 3 destructores, 4 fragatas) y "
                        + "dispara sobre el tablero enemigo: agua (X, pasa el turno), tocado (sigues "
                        + "disparando) u hundido (el barco completo queda marcado). "
                        + "Atajos: R gira el barco, ESPACIO coloca la flota al azar, V muestra el "
                        + "tablero enemigo (solo antes o despues de jugar, no hace trampa a mitad de "
                        + "partida) y ESC vuelve al menu principal.");
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }

    private void goToGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/batallanaval/view/main-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nicknameField.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
            stage.setResizable(true);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo cargar el tablero de juego.", e);
            messageLabel.setText("No se pudo iniciar la partida: " + e.getMessage());
        }
    }
}
