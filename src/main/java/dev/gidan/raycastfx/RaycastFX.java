package dev.gidan.raycastfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class RaycastFX extends Application {

    private static final String WINDOW_TITLE = "RaycastFX";

    private static final int GAME_WINDOW_WIDTH = 800;
    private static final int GAME_WINDOW_HEIGHT = 600;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(RaycastFX.class.getResource("game-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);
        Input.getInstance().init(scene);

        // Load the CSS file and apply it to the scene
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}