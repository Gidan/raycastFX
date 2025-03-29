package dev.gidan.raycastfx;

import dev.gidan.raycastfx.prefabs.Player;
import dev.gidan.raycastfx.util.Rectangle2DUtil;
import dev.gidan.raycastfx.util.Vec2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * This class is responsible to draw the minimap somewhere on the canvas.
 * It decides the position, the scale and the appearance of the minimap.
 */
public class MiniMap extends GameObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(MiniMap.class);

    private static final double INITIAL_CAMERA_POSITION_X = 0;
    private static final double INITIAL_CAMERA_POSITION_Y = 0;

    public static final Color GRID_LINE_COLOR = Color.rgb(60, 60, 60);

    private static final int INITIAL_MINIMAP_PLANE_WIDTH = 200;
    private static final int INITIAL_MINIMAP_PLANE_HEIGHT = 120;
    private static final int MINIMAP_PROJECTION_X = 20;
    private static final int MINIMAP_PROJECTION_Y = 20;
    private static final int GRID_SIZE = 20;

    private final int scale = 2;
    private final GraphicsContext gc;
    private final Player player;
    private final Set<GameObject> walls;

    private int minimapWidth = INITIAL_MINIMAP_PLANE_WIDTH * scale;
    private int minimapHeight = INITIAL_MINIMAP_PLANE_HEIGHT * scale;
    private final int gridCellSize = GRID_SIZE * scale;
    private final int centerPointRadius = 4 * scale;
    private boolean drawGrid = true;

    public MiniMap(Canvas canvas, Player player, Set<GameObject> walls) {
        super(Vec2D.of(INITIAL_CAMERA_POSITION_X, INITIAL_CAMERA_POSITION_Y));
        gc = canvas.getGraphicsContext2D();
        this.player = player;
        this.walls = walls;
    }

    private double calculateFaceDirectionAngle() {
        Vec2D worldMousePosition = calculateWorldMousePosition();
        double dx = this.position.getX() - worldMousePosition.getX();
        double dy = this.position.getY() - worldMousePosition.getY();
        return Math.atan2(dy, dx);
    }

    @Override
    public void update(double deltaTimeMillis) {
        // bind camera position to player's position
        this.position = player.position.multiply(scale);

        //this.faceDirection = player.faceDirection;
        double v = calculateFaceDirectionAngle();
        this.faceDirection = Vec2D.LEFT.rotate(v);

        // Draw a rectangle
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(MINIMAP_PROJECTION_X, MINIMAP_PROJECTION_Y, minimapWidth, minimapHeight);

        drawMouseCoordinates();
        drawWalls();
        drawGrid();
        drawCenterPoint();
        drawFaceDirection();
    }

    private void drawFaceDirection() {
        // draw center point
        gc.setStroke(Color.RED);

        double x = MINIMAP_PROJECTION_X + (double) minimapWidth / 2;
        double y = MINIMAP_PROJECTION_Y + (double) minimapHeight / 2;

        Vec2D d = Vec2D.of(x, y).add(faceDirection.multiply(20));

        gc.setLineWidth(2);
        gc.strokeLine(x, y, d.getX(), d.getY());
        gc.setLineWidth(1);
    }

    private void drawCenterPoint() {
        // draw center point
        gc.setFill(Color.RED);

        double x = MINIMAP_PROJECTION_X + (double) minimapWidth / 2 - centerPointRadius;
        double y = MINIMAP_PROJECTION_Y + (double) minimapHeight / 2 - centerPointRadius;
        double diameter = centerPointRadius * 2;

        gc.fillOval(x, y, diameter, diameter);
    }

    private void drawGrid() {
        if (drawGrid) {
            gc.setStroke(GRID_LINE_COLOR);

            int cameraPlaneHalfWidth = minimapWidth / 2;
            int cameraPlaneHalfHeight = minimapHeight / 2;

            for (int x = 0; x < minimapWidth; x++) {
                int i = (int) (position.getX()) - cameraPlaneHalfWidth + x;
                if (Math.abs(i) % gridCellSize == 0) {
                    gc.strokeLine(x + MINIMAP_PROJECTION_X,
                            MINIMAP_PROJECTION_Y,
                            x + MINIMAP_PROJECTION_X,
                            minimapHeight + MINIMAP_PROJECTION_Y);
                }
            }

            for (int y = 0; y < minimapHeight; y++) {
                int i = (int) (position.getY()) - cameraPlaneHalfHeight + y;
                if (Math.abs(i) % gridCellSize == 0) {
                    gc.strokeLine(MINIMAP_PROJECTION_X,
                            y + MINIMAP_PROJECTION_Y,
                            MINIMAP_PROJECTION_X + minimapWidth,
                            y + MINIMAP_PROJECTION_Y);
                }
            }
        }
    }

    private Vec2D calculateWorldMousePosition() {
        int mouseX = (int) Input.getInstance().getMouseX();
        int mouseY = (int) Input.getInstance().getMouseY();

        int cameraPlaneHalfWidth = minimapWidth / 2;
        int cameraPlaneHalfHeight = minimapHeight / 2;

        int worldX = (int) (position.getX()) - cameraPlaneHalfWidth + mouseX - MINIMAP_PROJECTION_X;
        int worldY = (int) (position.getY()) - cameraPlaneHalfHeight + mouseY - MINIMAP_PROJECTION_Y;

        return Vec2D.of(worldX, worldY);
    }

    private void drawMouseCoordinates() {
        int mouseX = (int) Input.getInstance().getMouseX();
        int mouseY = (int) Input.getInstance().getMouseY();

        Vec2D worldMousePosition = calculateWorldMousePosition();
        int worldX = (int) worldMousePosition.getX();
        int worldY = (int) worldMousePosition.getY();

        gc.setFill(Color.RED);
        gc.setFont(Fonts.TINY_NORMAL);
        gc.fillText(String.format("scene %d - %d", mouseX, mouseY),
                MINIMAP_PROJECTION_X,
                MINIMAP_PROJECTION_Y + minimapHeight + 15
        );
        gc.fillText(String.format("world %d - %d", worldX, worldY),
                MINIMAP_PROJECTION_X,
                MINIMAP_PROJECTION_Y + minimapHeight + 30
        );
    }

    private void drawWalls() {
        int cameraPlaneHalfWidth = minimapWidth / 2;
        int cameraPlaneHalfHeight = minimapHeight / 2;

        int minWorldX = (int) (position.getX()) - cameraPlaneHalfWidth;
        int minWorldY = (int) (position.getY()) - cameraPlaneHalfHeight;
        Rectangle2D miniMapArea = new Rectangle2D(minWorldX, minWorldY, minimapWidth, minimapHeight);
        gc.setFill(Color.DARKRED);
        walls.forEach(wall -> {
            double worldX = wall.position.getX() * gridCellSize;
            double worldY = wall.position.getY() * gridCellSize;
            Rectangle2D wallArea = new Rectangle2D(worldX, worldY, gridCellSize, gridCellSize);
            Rectangle2D intersection = Rectangle2DUtil.getIntersection(miniMapArea, wallArea);
            if (intersection != null) {
                gc.fillRect(
                        intersection.getMinX() + cameraPlaneHalfWidth + MINIMAP_PROJECTION_X - this.position.getX(),
                        intersection.getMinY() + cameraPlaneHalfHeight + MINIMAP_PROJECTION_Y - this.position.getY(),
                        intersection.getWidth(),
                        intersection.getHeight()
                );
            }
        });
    }

}
