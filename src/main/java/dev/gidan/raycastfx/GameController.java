package dev.gidan.raycastfx;

import dev.gidan.raycastfx.prefabs.Player;
import dev.gidan.raycastfx.prefabs.Wall;
import dev.gidan.raycastfx.util.Delta;
import dev.gidan.raycastfx.util.FPSCount;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class GameController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    @FXML
    public Canvas canvas;

    @FXML
    public void initialize() {
        Player player = new Player();
        Set<Wall> walls = Set.of(
                Wall.at(1, 1),
                Wall.at(2, 2),
                Wall.at(3, 3),
                Wall.at(2, -1),
                Wall.at(3, -1),
                Wall.at(4, -1)
        );

        MiniMap miniMap = new MiniMap(canvas, player, walls);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Start the animation timer
        AnimationTimer timer = new AnimationTimer() {
            private final FPSCount fpsCount = new FPSCount();
            private final Delta frameTime = new Delta();

            @Override
            public void handle(long nowInNano) {
                double delta = frameTime.seconds(nowInNano);

                // Clear the canvas
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                player.update(delta);
                miniMap.update(delta);
                drawFrameCount(delta);
            }

            private void drawFrameCount(final double delta) {
                gc.setFill(Color.YELLOW);
                gc.setFont(Fonts.SMALL_BOLD);
                int fps = fpsCount.frame(delta);
                gc.fillText(String.valueOf(fps), 20, canvas.getHeight() - 20);
            }

        };
        timer.start();
    }
}