package dev.gidan.raycastfx;

import dev.gidan.raycastfx.util.Vec2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniMap extends GameObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(MiniMap.class);

    private static final double INITIAL_CAMERA_POSITION_X = 0;
    private static final double INITIAL_CAMERA_POSITION_Y = 0;

    public static final Color GRID_LINE_COLOR = Color.rgb(60, 60, 60);

    private static final int INITIAL_CAMERA_PLANE_WIDTH = 200;
    private static final int INITIAL_CAMERA_PLANE_HEIGHT = 120;
    private static final int CAMERA_PROJECTION_X = 20;
    private static final int CAMERA_PROJECTION_Y = 20;

    private final int scale = 2;
    private final GraphicsContext gc;
    private final Player player;

    private int cameraPlaneWidth = INITIAL_CAMERA_PLANE_WIDTH * scale;
    private int cameraPlaneHeight = INITIAL_CAMERA_PLANE_HEIGHT * scale;
    private final int gridCellSize = 20 * scale;
    private final int centerPointRadius = 4 * scale;
    private boolean drawGrid = true;

    public MiniMap(Canvas canvas, Player player) {
        super(new Vec2D(INITIAL_CAMERA_POSITION_X, INITIAL_CAMERA_POSITION_Y));
        gc = canvas.getGraphicsContext2D();
        this.player = player;
    }

    @Override
    public void update(double deltaTimeMillis) {
        this.position = player.position.multiply(scale);
        this.faceDirection = player.faceDirection;

        // Draw a rectangle
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(CAMERA_PROJECTION_X, CAMERA_PROJECTION_Y, cameraPlaneWidth, cameraPlaneHeight);

        drawGrid(gc);
        drawCenterPoint(gc);
        drawFaceDirection(gc);
    }

    private void drawFaceDirection(GraphicsContext gc) {
        // draw center point
        gc.setStroke(Color.RED);

        double x = CAMERA_PROJECTION_X + (double) cameraPlaneWidth / 2;
        double y = CAMERA_PROJECTION_Y + (double) cameraPlaneHeight / 2;

        Vec2D d = new Vec2D(x, y).add(faceDirection.multiply(20));

        gc.setLineWidth(2);
        gc.strokeLine(x, y, d.getX(), d.getY());
        gc.setLineWidth(1);
    }

    private void drawCenterPoint(GraphicsContext gc) {
        // draw center point
        gc.setFill(Color.RED);

        double x = CAMERA_PROJECTION_X + (double) cameraPlaneWidth / 2 - centerPointRadius;
        double y = CAMERA_PROJECTION_Y + (double) cameraPlaneHeight / 2 - centerPointRadius;
        double diameter = centerPointRadius * 2;

        gc.fillOval(x, y, diameter, diameter);
    }

    private void drawGrid(GraphicsContext gc) {
        if (drawGrid) {
            gc.setStroke(GRID_LINE_COLOR);

            int cameraPlaneHalfWidth = cameraPlaneWidth / 2;
            int cameraPlaneHalfHeight = cameraPlaneHeight / 2;

            for (int x = 0; x < cameraPlaneWidth; x++) {
                int i = (int) (position.getX()) - cameraPlaneHalfWidth + x;
                if (Math.abs(i) % gridCellSize == 0) {
                    gc.strokeLine(x + CAMERA_PROJECTION_X,
                            CAMERA_PROJECTION_Y,
                            x + CAMERA_PROJECTION_X,
                            cameraPlaneHeight + CAMERA_PROJECTION_Y);
                }
            }

            for (int y = 0; y < cameraPlaneHeight; y++) {
                int i = (int) (position.getY()) - cameraPlaneHalfHeight + y;
                if (Math.abs(i) % gridCellSize == 0) {
                    gc.strokeLine(CAMERA_PROJECTION_X,
                            y + CAMERA_PROJECTION_Y,
                            CAMERA_PROJECTION_X + cameraPlaneWidth,
                            y + CAMERA_PROJECTION_Y);
                }
            }
        }
    }
}
