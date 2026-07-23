package com.batallanaval.controller;

import com.batallanaval.util.SoundManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the end-of-game screen (game-over-view.fxml): shows
 * whether the player won or lost and, when "Back to menu" is clicked,
 * replaces the current scene with the start screen (same navigation the
 * in-game "Back to menu" button already does).
 */
public class GameOverController {

    private static final Logger LOG = Logger.getLogger(GameOverController.class.getName());

    @FXML
    private Label titleLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Button backButton;

    public void setContent(String title, String message) {
        titleLabel.setText(title);
        messageLabel.setText(message);
    }

    @FXML
    private void onBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/batallanaval/view/start-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 690);
            SoundManager.attachButtonSounds(scene);
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo volver al menu principal.", e);
        }
    }
}
