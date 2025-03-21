package dev.gidan.raycastfx;

import dev.gidan.raycastfx.util.Vec2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Camera {

    private static final Logger LOGGER = LoggerFactory.getLogger(Camera.class);

    public static final Color GRID_LINE_COLOR = Color.rgb(60, 60, 60);

    private static final double INITIAL_CAMERA_POSITION_X = 0;
    private static final double INITIAL_CAMERA_POSITION_Y = 0;
    private static final int INITIAL_CAMERA_PLANE_WIDTH = 200;
    private static final int INITIAL_CAMERA_PLANE_HEIGHT = 120;
    private static final int CAMERA_PROJECTION_X = 20;
    private static final int CAMERA_PROJECTION_Y = 20;

    private final int scale = 2;

    private Vec2D position;
    private Vec2D faceDirection;
    private int cameraPlaneWidth = INITIAL_CAMERA_PLANE_WIDTH * scale;
    private int cameraPlaneHeight = INITIAL_CAMERA_PLANE_HEIGHT * scale;
    private final int gridCellSize = 20 * scale;
    private final int centerPointRadius = 4 * scale;
    private int speed = 20 * scale;

    private boolean drawGrid = true;

    public Camera() {
        position = new Vec2D(INITIAL_CAMERA_POSITION_X, INITIAL_CAMERA_POSITION_Y);
        faceDirection = Vec2D.UP;
    }

    public void update(Canvas canvas, double deltaTimeMillis) {
        updateWorldPosition(deltaTimeMillis);
        updateFaceDirection();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw a rectangle
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(CAMERA_PROJECTION_X, CAMERA_PROJECTION_Y, cameraPlaneWidth, cameraPlaneHeight);

        drawGrid(gc);
        drawCenterPoint(gc);
        drawFaceDirection(gc);
    }

    private void updateWorldPosition(double deltaTime) {
        Input input = Input.getInstance();
        Vec2D direction = Vec2D.ZERO;
        if (input.isPressed(KeyCode.W)) {
            direction = direction.add(Vec2D.UP);
        }
        if (input.isPressed(KeyCode.S)) {
            direction = direction.add(Vec2D.DOWN);
        }
        if (input.isPressed(KeyCode.A)) {
            direction = direction.add(Vec2D.LEFT);
        }
        if (input.isPressed(KeyCode.D)) {
            direction = direction.add(Vec2D.RIGHT);
        }

        if (direction.magnitude() > 0) {
            position = position.add(direction.multiply(deltaTime * speed));
        }
    }

    private void updateFaceDirection() {
        Input input = Input.getInstance();

        double angle = Math.toRadians(1);

        if (input.isPressed(KeyCode.E)) {
            faceDirection = faceDirection.rotate(angle);
        }
        if (input.isPressed(KeyCode.Q)) {
            faceDirection = faceDirection.rotate(-angle);
        }
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
            for (int x = 0; x < cameraPlaneWidth; x++) {
                int i = (int)(position.getX()) - cameraPlaneHalfWidth + x;
                if (Math.abs(i) % gridCellSize == 0) {
                    gc.strokeLine(x + CAMERA_PROJECTION_X,
                            CAMERA_PROJECTION_Y,
                            x + CAMERA_PROJECTION_X,
                            cameraPlaneHeight + CAMERA_PROJECTION_Y);
                }
            }

            int cameraPlaneHalfHeight = cameraPlaneHeight / 2;
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
