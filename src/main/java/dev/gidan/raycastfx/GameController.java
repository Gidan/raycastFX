package dev.gidan.raycastfx;

import dev.gidan.raycastfx.prefabs.Player;
import dev.gidan.raycastfx.prefabs.Wall;
import dev.gidan.raycastfx.util.Vec2D;
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
        Set<GameObject> walls = Set.of(Wall.at(1, 1), Wall.at(2, -1));

        MiniMap miniMap = new MiniMap(canvas, player, walls);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Start the animation timer
        AnimationTimer timer = new AnimationTimer() {
            long start = 0L;

            final int updateFpsCountFrameSkip = 60;
            int frameCount = 0;
            int fps = 0;

            @Override
            public void handle(long nowInNano) {
                frameCount++;
                long nowInMillis = nowInNano / 1_000_000;

                if (start == 0L) {
                    start = nowInMillis;
                }
                double delta = (nowInMillis - start) / 1000.0;
                start = nowInMillis;

                // Clear the canvas
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                player.update(delta);
                miniMap.update(delta);

                drawFrameCount(delta);
            }

            private void drawFrameCount(final double delta) {
                gc.setFill(Color.YELLOW);
                gc.setFont(Fonts.SMALL_BOLD);
                if (frameCount >= updateFpsCountFrameSkip) {
                    frameCount = 0;
                    fps = (int)(1.0 / delta);
                }
                gc.fillText(String.valueOf(fps), 20, canvas.getHeight() - 20);
            }

        };
        timer.start();
    }
}