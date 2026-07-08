package com.batallanaval.controller;

import com.batallanaval.exception.PersistenciaException;
import com.batallanaval.persistence.GamePersistenceManager;
import com.batallanaval.util.SesionJuego;

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
    private Button continuarButton;

    @FXML
    private Label mensajeLabel;

    private final GamePersistenceManager persistencia = new GamePersistenceManager();

    @FXML
    public void initialize() {
        continuarButton.setDisable(!persistencia.existePartidaGuardada());
    }

    @FXML
    private void onJugar() {
        SesionJuego.setNicknameHumano(nicknameField.getText());
        try {
            persistencia.eliminarPartidaGuardada();
        } catch (PersistenciaException e) {
            LOG.log(Level.WARNING, "No se pudo borrar la partida guardada anterior.", e);
        }
        irAlJuego();
    }

    @FXML
    private void onContinuar() {
        irAlJuego();
    }

    @FXML
    private void onOpciones() {
        mensajeLabel.setTextFill(Color.web("#F5E6C8"));
        mensajeLabel.setText("Las opciones todavia no estan disponibles.");
    }

    @FXML
    private void onSalir() {
        Platform.exit();
    }

    private void irAlJuego() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/batallanaval/view/main-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nicknameField.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
            stage.setResizable(true);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo cargar el tablero de juego.", e);
            mensajeLabel.setText("No se pudo iniciar la partida: " + e.getMessage());
        }
    }
}
