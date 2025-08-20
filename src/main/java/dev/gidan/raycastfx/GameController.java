package dev.gidan.raycastfx;

import dev.gidan.raycastfx.util.Delta;
import dev.gidan.raycastfx.util.FPSCount;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameController {

    @FXML
    public Canvas canvas;

    @FXML
    public void initialize() {
        final GameState gameState = new GameState();

        final MiniMap miniMap = new MiniMap(canvas, gameState);
        final GameFrameRenderer gameFrameRenderer = new GameFrameRenderer(canvas, gameState);

        final GraphicsContext gc = canvas.getGraphicsContext2D();

        // Start the animation timer
        AnimationTimer timer = new AnimationTimer() {
            private final FPSCount fpsCount = new FPSCount();
            private final Delta frameTime = new Delta();

            @Override
            public void handle(long nowInNano) {
                double delta = frameTime.seconds(nowInNano);
                gameState.update(delta);

                // Clear the canvas
                clearFrame();

                gameFrameRenderer.update(delta);
                miniMap.update(delta);
                drawFrameCount(delta);
            }

            private void clearFrame() {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
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