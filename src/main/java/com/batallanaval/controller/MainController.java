package com.batallanaval.controller;

import com.batallanaval.util.SoundManager;

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
 * Controller for the main view (FXML). Its only responsibility is to
 * connect the interface components with the {@link GameController}, which
 * holds all the actual game logic.
 */
public class MainController {

    private static final Logger LOG = Logger.getLogger(MainController.class.getName());

    /** CSS pseudo-class that marks the compass as active (verification mode on). */
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

    @FXML
    private Button backButton;

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
     * Keyboard shortcuts: R rotates the current ship, SPACE places the
     * random fleet, V toggles verification of the enemy board, and ESCAPE
     * goes back to the main menu. Instead of calling the controller's
     * method directly, the corresponding button is fired (Button.fire()):
     * this way the shortcut behaves identically to a mouse click in every
     * respect, including the sound (SoundManager attaches the click sound
     * to the button's ActionEvent, not to the key) and respecting the
     * disabled state (fire() does nothing if the button is disabled).
     */
    private void onKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case R -> rotateButton.fire();
            case SPACE -> randomFleetButton.fire();
            case V -> compassButton.fire();
            case ESCAPE -> backButton.fire();
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
     * "Emergency exit" to the main menu (usability heuristic "user control
     * and freedom"): the game already autosaves after every move, so the
     * player can leave without losing progress and resume it later with
     * "Travel log".
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
            Scene scene = new Scene(root, 1280, 690);
            SoundManager.attachButtonSounds(scene);
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo volver al menu principal.", e);
            statusLabel.setText("No se pudo volver al menu: " + e.getMessage());
        }
    }
}
