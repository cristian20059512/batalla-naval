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

    /** Nickname con el que se guardo la ultima partida, o null si no hay ninguna (o no se pudo leer). */
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
     * "Continuar" solo se habilita si el nombre escrito coincide con el
     * nickname de la partida guardada (para no cargar por error/trampa la
     * partida de otra persona que haya jugado antes en el mismo equipo).
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
