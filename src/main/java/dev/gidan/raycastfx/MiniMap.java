package dev.gidan.raycastfx;

import dev.gidan.raycastfx.prefabs.Player;
import dev.gidan.raycastfx.prefabs.Wall;
import dev.gidan.raycastfx.util.GridUtil;
import dev.gidan.raycastfx.util.Rectangle2DUtil;
import dev.gidan.raycastfx.util.Trig;
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

    public static final double EPSILON = 0.03;
    public static final double INFINITE_DISTANCE = 1_000_000;

    private final int scale = 1;
    private final GraphicsContext gc;
    private final Player player;
    private final Set<Wall> walls;
    private final Canvas canvas;

    private int minimapWidth = INITIAL_MINIMAP_PLANE_WIDTH * scale;
    private int minimapHeight = INITIAL_MINIMAP_PLANE_HEIGHT * scale;
    private final int gridCellSize = GRID_SIZE * scale;
    private final int centerPointRadius = 4 * scale;
    private boolean drawGrid = true;

    int minimapPlaneHalfWidth = minimapWidth / 2;
    int minimapPlaneHalfHeight = minimapHeight / 2;
    Vec2D minimapSize = Vec2D.of(minimapWidth, minimapHeight);
    Vec2D minimapOffset = Vec2D.of(MINIMAP_PROJECTION_X, MINIMAP_PROJECTION_Y);

    private double fovAngle = 120;
    private int lines = 90;

    public MiniMap(Canvas canvas, Player player, Set<Wall> walls) {
        super(Vec2D.of(INITIAL_CAMERA_POSITION_X, INITIAL_CAMERA_POSITION_Y));
        gc = canvas.getGraphicsContext2D();
        this.canvas = canvas;
        this.player = player;
        this.walls = walls;
    }

    private double calculateFaceDirectionAngle() {
        Vec2D worldMousePosition = calculateWorldMousePosition();
        return Vec2D.Util.angle(this.position, worldMousePosition);
    }

    @Override
    public void update(double deltaTimeMillis) {
        // bind camera position to player's position
        this.position = player.position.multiply(scale);

        //this.faceDirection = player.faceDirection;
        double faceDirectionAngle = calculateFaceDirectionAngle();
        this.player.rotation = Vec2D.LEFT.rotate(faceDirectionAngle);

        // Draw minimap background
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(MINIMAP_PROJECTION_X, MINIMAP_PROJECTION_Y, minimapWidth, minimapHeight);

        //double distance = rayCast(position, player.rotation);
        //drawRaycastDistance(distance);

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        double lineWidth = canvasWidth / lines;
        double anglePerLine = fovAngle / lines;
        double startAngle = (fovAngle/2) * -1;

        for (int ray = 0; ray < lines; ray++) {
            double angle = startAngle + (anglePerLine * ray);
            double distance = rayCast(position, player.rotation.rotate(Math.toRadians(angle)));
            if (distance < INFINITE_DISTANCE) {
                double x = ray * lineWidth;
                double lineHeight = (1 / Math.max(distance, 1)) * canvasHeight * 10 ;
                Color color = Color.hsb(Color.RED.getHue(), Color.RED.getSaturation(), Math.max(0, Math.min(1.0, 1 / distance * 10)));
                gc.setStroke(color);
                gc.setLineWidth(lineWidth + 2);
                gc.strokeLine(x, canvasHeight / 2 - lineHeight / 2, x, canvasHeight / 2 + lineHeight / 2);
            }
        }

        drawMouseCoordinates();
        drawPlayerPosition();
        drawWalls();
        drawGrid();
        drawCenterPoint();
        //drawFaceDirection();
        drawPlayerRotationAngle();
    }

    private void drawFaceDirection() {
        // draw center point
        gc.setStroke(Color.RED);

        // the player is always centered in the minimap.
        // So x, y can just be the center of the minimap plus the minimap offset.
        Vec2D origin = minimapSize.multiply(0.5).add(minimapOffset);
        Vec2D direction = origin.add(player.rotation.multiply(20));

        gc.setLineWidth(2);
        gc.strokeLine(origin.getX(), origin.getY(), direction.getX(), direction.getY());
        gc.setLineWidth(1);
    }

    private void drawCenterPoint() {
        // draw center point
        gc.setFill(Color.RED);

        double x = MINIMAP_PROJECTION_X + minimapPlaneHalfWidth - centerPointRadius;
        double y = MINIMAP_PROJECTION_Y + minimapPlaneHalfHeight - centerPointRadius;
        double diameter = centerPointRadius * 2;

        gc.fillOval(x, y, diameter, diameter);
    }

    private void drawGrid() {
        if (drawGrid) {
            gc.setLineWidth(1);
            gc.setStroke(GRID_LINE_COLOR);

            double posX = position.getX();
            double posY = position.getY();

            for (int x = 0; x < minimapWidth; x++) {
                int i = (int) posX - minimapPlaneHalfWidth + x;
                if (Math.abs(i) % gridCellSize == 0) {
                    gc.strokeLine(x + MINIMAP_PROJECTION_X,
                            MINIMAP_PROJECTION_Y,
                            x + MINIMAP_PROJECTION_X,
                            minimapHeight + MINIMAP_PROJECTION_Y);
                }
            }

            for (int y = 0; y < minimapHeight; y++) {
                int i = (int) posY - minimapPlaneHalfHeight + y;
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
        return sceneToWorld(Input.getInstance().mouse());
    }

    private void drawMouseCoordinates() {
        int mouseX = (int) Input.getInstance().getMouseX();
        int mouseY = (int) Input.getInstance().getMouseY();

        Vec2D worldMousePosition = calculateWorldMousePosition();
        int worldX = (int) worldMousePosition.getX();
        int worldY = (int) worldMousePosition.getY();

        gc.setFill(Color.RED);
        gc.setFont(Fonts.TINY_NORMAL);
        gc.fillText(String.format("mouse scene %d - %d", mouseX, mouseY),
                MINIMAP_PROJECTION_X,
                MINIMAP_PROJECTION_Y + minimapHeight + 15
        );
        gc.fillText(String.format("mouse world %d - %d", worldX, worldY),
                MINIMAP_PROJECTION_X,
                MINIMAP_PROJECTION_Y + minimapHeight + 30
        );
    }

    private void drawPlayerPosition() {
        Vec2D playerPosition = this.position;
        Vec2D playerScenePosition = worldToScene(playerPosition);
        gc.setFill(Color.RED);
        gc.setFont(Fonts.TINY_NORMAL);
        gc.fillText(String.format("player world x:%.02f y:%.02f", playerPosition.getX(), playerPosition.getY()),
                MINIMAP_PROJECTION_X,
                MINIMAP_PROJECTION_Y + minimapHeight + 45
        );
        gc.fillText(String.format("player scene x:%.02f y:%.02f", playerScenePosition.getX(), playerScenePosition.getY()),
                MINIMAP_PROJECTION_X,
                MINIMAP_PROJECTION_Y + minimapHeight + 60
        );
    }

    private void drawPlayerRotationAngle() {
        Vec2D playerRotation = player.rotation;
        double rotationAngle = playerRotation.angle();
        gc.setFill(Color.RED);
        gc.setFont(Fonts.TINY_NORMAL);
        gc.fillText(String.format("player rot angle rad:%.02f deg:%.02f", rotationAngle, Math.toDegrees(rotationAngle)),
                MINIMAP_PROJECTION_X,
                MINIMAP_PROJECTION_Y + minimapHeight + 75
        );
    }

    private void drawRaycastDistance(double distance) {
        gc.setFill(Color.RED);
        gc.setFont(Fonts.TINY_NORMAL);
        gc.fillText(String.format("raycast distance:%.02f", distance),
                MINIMAP_PROJECTION_X,
                MINIMAP_PROJECTION_Y + minimapHeight + 90
        );
    }

    private Rectangle2D getMiniMapWorldBounds() {
        int minWorldX = (int) (position.getX()) - minimapPlaneHalfWidth;
        int minWorldY = (int) (position.getY()) - minimapPlaneHalfHeight;
        return new Rectangle2D(minWorldX, minWorldY, minimapWidth, minimapHeight);
    }

    private void drawWalls() {
        Rectangle2D miniMapArea = getMiniMapWorldBounds();
        gc.setFill(Color.DARKRED);
        walls.forEach(wall -> {
            double worldX = wall.position.getX() * gridCellSize;
            double worldY = wall.position.getY() * gridCellSize;
            Rectangle2D wallArea = new Rectangle2D(worldX, worldY, gridCellSize, gridCellSize);
            Rectangle2D intersection = Rectangle2DUtil.getIntersection(miniMapArea, wallArea);
            if (intersection != null) {
                gc.fillRect(
                        intersection.getMinX() + minimapPlaneHalfWidth + MINIMAP_PROJECTION_X - this.position.getX(),
                        intersection.getMinY() + minimapPlaneHalfHeight + MINIMAP_PROJECTION_Y - this.position.getY(),
                        intersection.getWidth(),
                        intersection.getHeight()
                );
            }
        });
    }

    private double rayCast(Vec2D origin, Vec2D direction) {
        double originX = origin.getX();
        double originY = origin.getY();

        Rectangle2D miniMapWorldBounds = getMiniMapWorldBounds();

        if (!Rectangle2DUtil.containsPoint(miniMapWorldBounds, origin)) {
            return INFINITE_DISTANCE;
        }

        boolean isColliding = walls.stream().map(w -> w.position.multiply(gridCellSize))
                .filter(wallPosition -> Rectangle2DUtil.containsPoint(miniMapWorldBounds, wallPosition))
                .map(w -> new Rectangle2D(w.getX(), w.getY(), gridCellSize, gridCellSize))
                .anyMatch(wallBounds -> Rectangle2DUtil.containsPoint(wallBounds, origin));

        if (isColliding) {
            return -1;
        }

        double minX = GridUtil.nearestMultipleBelow(originX, gridCellSize);
        double minY = GridUtil.nearestMultipleBelow(originY, gridCellSize);
        double maxX = GridUtil.nearestMultipleAbove(originX, gridCellSize);
        double maxY = GridUtil.nearestMultipleAbove(originY, gridCellSize);

        Vec2D topLeft = Vec2D.of(minX, minY);
        Vec2D topRight = Vec2D.of(maxX, minY);
        Vec2D bottomRight = Vec2D.of(maxX, maxY);
        Vec2D bottomLeft = Vec2D.of(minX, maxY);

        Vec2D topLeftScene = worldToScene(topLeft);
        Vec2D topRightScene = worldToScene(topRight);
        Vec2D bottomRightScene = worldToScene(bottomRight);
        Vec2D bottomLeftScene = worldToScene(bottomLeft);

        gc.setFill(Color.BLUE);
        gc.fillOval(topLeftScene.getX() -2, topLeftScene.getY() -2, 4, 4);
        gc.fillOval(topRightScene.getX() -2, topRightScene.getY() -2, 4, 4);
        gc.fillOval(bottomRightScene.getX() -2, bottomRightScene.getY() -2, 4, 4);
        gc.fillOval(bottomLeftScene.getX() -2, bottomLeftScene.getY() -2, 4, 4);

        double topRightAngle = -(Math.PI - Vec2D.Util.angle(origin, topRight));
        double topLeftAngle = -(Math.PI - Vec2D.Util.angle(origin, topLeft));
        double bottomRightAngle = Math.PI + Vec2D.Util.angle(origin, bottomRight);
        double bottomLeftAngle = Math.PI + Vec2D.Util.angle(origin, bottomLeft);

        double rotationAngle = direction.angle();
        double x, y;

        if (rotationAngle < 0) {
            if (rotationAngle > topRightAngle) {
                x = maxX;
                y = originY + Trig.tan(rotationAngle) * Math.abs(originX - maxX);
            } else if (rotationAngle < topLeftAngle) {
                x = minX;
                y = originY - (Trig.tan(rotationAngle) * Math.abs(originX - minX));
            } else {
                x = originX - Trig.cotan(rotationAngle) * Math.abs(originY - minY);
                y = minY;
            }
        } else {
            if (rotationAngle < bottomRightAngle) {
                x = maxX;
                y = originY + Trig.tan(rotationAngle) * Math.abs(originX - maxX);
            } else if (rotationAngle < bottomLeftAngle) {
                x = originX + Trig.cotan(rotationAngle) * Math.abs(originY - maxY);
                y = maxY;
            } else {
                x = minX;
                y = originY - (Trig.tan(rotationAngle) * Math.abs(originX - minX));
            }
        }

        Vec2D collision = Vec2D.of(x, y);
        Vec2D scenePosition = worldToScene(collision);
        Vec2D originScene = worldToScene(origin);

        gc.setFill(Color.RED);
        gc.fillOval(scenePosition.getX() -3, scenePosition.getY() -3, 6, 6);

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeLine(originScene.getX(), originScene.getY(), scenePosition.getX(), scenePosition.getY());
        gc.setLineWidth(1);

        double distance = Math.abs(origin.subtract(collision).magnitude());
        double nextDistance = rayCast(collision.add(direction.multiply(EPSILON)), direction);
        if (nextDistance > 0) {
            distance += nextDistance;
        }

        return Math.min(distance, INFINITE_DISTANCE);
    }

    private Vec2D worldToScene(Vec2D world) {
        return world.subtract(position)
                .add(minimapSize.multiply(0.5))
                .add(minimapOffset);
    }

    private Vec2D sceneToWorld(Vec2D scene) {
        return Vec2D.of(position.getX(), position.getY())
                .add(scene)
                .subtract(minimapSize.multiply(0.5).add(minimapOffset));
    }



}
