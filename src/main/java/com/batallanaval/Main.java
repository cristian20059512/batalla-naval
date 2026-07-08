package com.batallanaval;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada de la aplicacion Batalla Naval.
 * Carga la vista principal desde FXML y arranca el ciclo de eventos de JavaFX.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/com/batallanaval/view/main-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 650);
        stage.setTitle("Batalla Naval");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
