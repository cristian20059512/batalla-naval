package com.batallanaval.controller;

import com.batallanaval.exception.PersistenceException;
import com.batallanaval.persistence.GamePersistenceManager;
import com.batallanaval.util.GameSession;
import com.batallanaval.util.SoundManager;

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
 * Controller for the start screen (FXML): asks for the player's nickname
 * and offers "Play" (new game) or "Continue" (resumes the last saved game,
 * see {@link GamePersistenceManager}).
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

    /** Nickname the last game was saved with, or null if there is none (or it couldn't be read). */
    private String savedNickname;

    @FXML
    public void initialize() {
        if (persistenceManager.hasSavedGame()) {
            try {
                savedNickname = persistenceManager.loadSummary().getHumanNickname();
            } catch (PersistenceException e) {
                LOG.log(Level.WARNING, "No se pudo leer el resumen de la partida guardada.", e);
                savedNickname = null;
            }
        }
        updateContinueAvailability();
        nicknameField.textProperty().addListener((observable, oldValue, newValue) -> updateContinueAvailability());
    }

    /**
     * "Continue" is only enabled if the typed name matches the saved
     * game's nickname (so that another person's game, played earlier on
     * the same team, isn't loaded by mistake or on purpose).
     */
    private void updateContinueAvailability() {
        boolean matches = savedNickname != null
                && nicknameField.getText() != null
                && nicknameField.getText().trim().equalsIgnoreCase(savedNickname.trim());
        continueButton.setDisable(!matches);
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
        // el boton ya deberia estar deshabilitado si no coincide, pero se
        // revalida aqui por si se invocara de otra forma (defensa en
        // profundidad, igual que canVerify() en GameController).
        if (savedNickname == null || nicknameField.getText() == null
                || !nicknameField.getText().trim().equalsIgnoreCase(savedNickname.trim())) {
            messageLabel.setTextFill(Color.web("#F0997B"));
            messageLabel.setText("Escribe el mismo nombre con el que guardaste la partida para continuar.");
            return;
        }
        GameSession.setHumanNickname(nicknameField.getText());
        goToGame();
    }

    /**
     * Shows the game instructions (usability heuristic "help and
     * documentation"): basic rules, terminology, and keyboard shortcuts, so
     * the player doesn't have to guess them by trial and error.
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
            // 1320x880: los dos tableros son SubScene 3D de 600x520 cada
            // uno; con el espaciado y el padding del HBox, un tamano menor
            // los recorta.
            Scene scene = new Scene(root, 1320, 880);
            SoundManager.attachButtonSounds(scene);
            stage.setScene(scene);
            stage.setResizable(true);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo cargar el tablero de juego.", e);
            messageLabel.setText("No se pudo iniciar la partida: " + e.getMessage());
        }
    }
}
