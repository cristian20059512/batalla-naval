package com.batallanaval;

import com.batallanaval.util.SoundManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Entry point for the Battleship application.
 * Loads the main view from FXML and starts the JavaFX event loop.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/batallanaval/view/start-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 690);
        SoundManager.attachButtonSounds(scene);
        SoundManager.playBackgroundMusic();

        stage.setTitle("Batalla Naval");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
