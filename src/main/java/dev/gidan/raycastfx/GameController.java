package dev.gidan.raycastfx;

import dev.gidan.raycastfx.prefabs.Player;
import dev.gidan.raycastfx.prefabs.Wall;
import dev.gidan.raycastfx.util.Delta;
import dev.gidan.raycastfx.util.FPSCount;
import dev.gidan.raycastfx.util.Vec2D;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class GameController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    @FXML
    public Canvas canvas;

    private Set<Wall> loadMap() {
        Image image = new Image(Objects.requireNonNull(getClass().getResource("/map.png")).toExternalForm());

        PixelReader pixelReader = image.getPixelReader();
        double width = image.getWidth();
        double height = image.getHeight();

        Set<Wall> walls = new HashSet<>();

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Color color = pixelReader.getColor(c, r);
                if (color.equals(Color.BLACK)) {
                    walls.add(Wall.at(c, r));
                }
            }
        }

        return walls;
    }

    private Vec2D loadPlayer() {
        Image image = new Image(Objects.requireNonNull(getClass().getResource("/map.png")).toExternalForm());

        PixelReader pixelReader = image.getPixelReader();
        double width = image.getWidth();
        double height = image.getHeight();

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Color color = pixelReader.getColor(c, r);
                if (color.equals(Color.RED)) {
                    return Vec2D.of(r,c);
                }
            }
        }

        return Player.DEFAULT_POSITION;
    }

    @FXML
    public void initialize() {
        Player player = new Player(loadPlayer());
        Set<Wall> walls = loadMap();

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