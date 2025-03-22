package dev.gidan.raycastfx;

import dev.gidan.raycastfx.prefabs.Player;
import dev.gidan.raycastfx.util.Vec2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class MiniMap extends GameObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(MiniMap.class);

    private static final double INITIAL_CAMERA_POSITION_X = 0;
    private static final double INITIAL_CAMERA_POSITION_Y = 0;

    public static final Color GRID_LINE_COLOR = Color.rgb(60, 60, 60);

    private static final int INITIAL_CAMERA_PLANE_WIDTH = 200;
    private static final int INITIAL_CAMERA_PLANE_HEIGHT = 120;
    private static final int MINIMAP_PROJECTION_X = 20;
    private static final int MINIMAP_PROJECTION_Y = 20;
    private static final int GRID_SIZE = 20;

    private final int scale = 2;
    private final GraphicsContext gc;
    private final Player player;
    private final Set<GameObject> walls;

    private int cameraPlaneWidth = INITIAL_CAMERA_PLANE_WIDTH * scale;
    private int cameraPlaneHeight = INITIAL_CAMERA_PLANE_HEIGHT * scale;
    private final int gridCellSize = GRID_SIZE * scale;
    private final int centerPointRadius = 4 * scale;
    private boolean drawGrid = true;

    public MiniMap(Canvas canvas, Player player, Set<GameObject> walls) {
        super(Vec2D.of(INITIAL_CAMERA_POSITION_X, INITIAL_CAMERA_POSITION_Y));
        gc = canvas.getGraphicsContext2D();
        this.player = player;
        this.walls = walls;
    }

    @Override
    public void update(double deltaTimeMillis) {
        this.position = player.position.multiply(scale);
        this.faceDirection = player.faceDirection;

        // Draw a rectangle
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(MINIMAP_PROJECTION_X, MINIMAP_PROJECTION_Y, cameraPlaneWidth, cameraPlaneHeight);

        drawWalls();
        drawGrid();
        drawCenterPoint();
        drawFaceDirection();
        drawMouseCoordinates();
    }

    private void drawFaceDirection() {
        // draw center point
        gc.setStroke(Color.RED);

        double x = MINIMAP_PROJECTION_X + (double) cameraPlaneWidth / 2;
        double y = MINIMAP_PROJECTION_Y + (double) cameraPlaneHeight / 2;

        Vec2D d = Vec2D.of(x, y).add(faceDirection.multiply(20));

        gc.setLineWidth(2);
        gc.strokeLine(x, y, d.getX(), d.getY());
        gc.setLineWidth(1);
    }

    private void drawCenterPoint() {
        // draw center point
        gc.setFill(Color.RED);

        double x = MINIMAP_PROJECTION_X + (double) cameraPlaneWidth / 2 - centerPointRadius;
        double y = MINIMAP_PROJECTION_Y + (double) cameraPlaneHeight / 2 - centerPointRadius;
        double diameter = centerPointRadius * 2;

        gc.fillOval(x, y, diameter, diameter);
    }

    private void drawGrid() {
        if (drawGrid) {
            gc.setStroke(GRID_LINE_COLOR);

            int cameraPlaneHalfWidth = cameraPlaneWidth / 2;
            int cameraPlaneHalfHeight = cameraPlaneHeight / 2;

            for (int x = 0; x < cameraPlaneWidth; x++) {
                int i = (int) (position.getX()) - cameraPlaneHalfWidth + x;
                if (Math.abs(i) % gridCellSize == 0) {
                    gc.strokeLine(x + MINIMAP_PROJECTION_X,
                            MINIMAP_PROJECTION_Y,
                            x + MINIMAP_PROJECTION_X,
                            cameraPlaneHeight + MINIMAP_PROJECTION_Y);
                }
            }

            for (int y = 0; y < cameraPlaneHeight; y++) {
                int i = (int) (position.getY()) - cameraPlaneHalfHeight + y;
                if (Math.abs(i) % gridCellSize == 0) {
                    gc.strokeLine(MINIMAP_PROJECTION_X,
                            y + MINIMAP_PROJECTION_Y,
                            MINIMAP_PROJECTION_X + cameraPlaneWidth,
                            y + MINIMAP_PROJECTION_Y);
                }
            }
        }
    }

    private void drawMouseCoordinates() {
        int mouseX = (int) Input.getInstance().getMouseX();
        int mouseY = (int) Input.getInstance().getMouseY();

        int cameraPlaneHalfWidth = cameraPlaneWidth / 2;
        int cameraPlaneHalfHeight = cameraPlaneHeight / 2;

        int worldX = (int) (position.getX()) - cameraPlaneHalfWidth + mouseX - MINIMAP_PROJECTION_X;
        int worldY = (int) (position.getY()) - cameraPlaneHalfHeight + mouseY - MINIMAP_PROJECTION_Y;

        gc.setFill(Color.RED);
        gc.setFont(Fonts.TINY_NORMAL);
        gc.fillText(String.format("scene %d - %d", mouseX, mouseY),
                MINIMAP_PROJECTION_X,
                MINIMAP_PROJECTION_Y + cameraPlaneHeight + 15
        );
        gc.fillText(String.format("world %d - %d", worldX, worldY),
                MINIMAP_PROJECTION_X,
                MINIMAP_PROJECTION_Y + cameraPlaneHeight + 30
        );
    }

    private void drawWalls() {
        int cameraPlaneHalfWidth = cameraPlaneWidth / 2;
        int cameraPlaneHalfHeight = cameraPlaneHeight / 2;

        int minWorldX = (int) (position.getX()) - cameraPlaneHalfWidth;
        int minWorldY = (int) (position.getY()) - cameraPlaneHalfHeight;
        Rectangle2D miniMapArea = new Rectangle2D(minWorldX, minWorldY, cameraPlaneWidth, cameraPlaneHeight);
        gc.setFill(Color.DARKRED);
        walls.forEach(wall -> {
            double worldX = wall.position.getX() * gridCellSize;
            double worldY = wall.position.getY() * gridCellSize;
            Rectangle2D wallArea = new Rectangle2D(worldX, worldY, gridCellSize, gridCellSize);
            Rectangle2D intersection = getIntersection(miniMapArea, wallArea);
            if (intersection != null) {
                gc.fillRect(
                        intersection.getMinX() + cameraPlaneHalfWidth + MINIMAP_PROJECTION_X - position.getX(),
                        intersection.getMinY() + cameraPlaneHalfHeight + MINIMAP_PROJECTION_Y - position.getY(),
                        intersection.getWidth(),
                        intersection.getHeight()
                        );
            }
        });
    }

    public static Rectangle2D getIntersection(Rectangle2D rect1, Rectangle2D rect2) {
        double xOverlap = Math.max(0, Math.min(rect1.getMinX() + rect1.getWidth(), rect2.getMinX() + rect2.getWidth()) - Math.max(rect1.getMinX(), rect2.getMinX()));
        double yOverlap = Math.max(0, Math.min(rect1.getMinY() + rect1.getHeight(), rect2.getMinY() + rect2.getHeight()) - Math.max(rect1.getMinY(), rect2.getMinY()));

        if (xOverlap > 0 && yOverlap > 0) {
            double intersectionX = Math.max(rect1.getMinX(), rect2.getMinX());
            double intersectionY = Math.max(rect1.getMinY(), rect2.getMinY());
            return new Rectangle2D(intersectionX, intersectionY, xOverlap, yOverlap);
        } else {
            return null; // No intersection
        }
    }


}
