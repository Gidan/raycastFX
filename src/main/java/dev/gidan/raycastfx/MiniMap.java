package dev.gidan.raycastfx;

import dev.gidan.raycastfx.prefabs.Player;
import dev.gidan.raycastfx.prefabs.Wall;
import dev.gidan.raycastfx.util.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

/**
 * This class is responsible to draw the minimap somewhere on the canvas.
 * It decides the position, the scale and the appearance of the minimap.
 */
@Slf4j
public class MiniMap extends GameObject {

    private static final double INITIAL_CAMERA_POSITION_X = 0;
    private static final double INITIAL_CAMERA_POSITION_Y = 0;
    public static final Color GRID_LINE_COLOR = Color.rgb(60, 60, 60);
    private static final int INITIAL_MINIMAP_PLANE_WIDTH = 200;
    private static final int INITIAL_MINIMAP_PLANE_HEIGHT = 120;
    private static final int MINIMAP_PROJECTION_X = 20;
    private static final int MINIMAP_PROJECTION_Y = 20;
    public static final int GRID_SIZE = 20;
    public static final double EPSILON = 0.03;

    private final int scale = 1;
    private final GraphicsContext gc;
    private final Player player;
    private final GameState gameState;

    private final int minimapWidth = INITIAL_MINIMAP_PLANE_WIDTH * scale;
    private final int minimapHeight = INITIAL_MINIMAP_PLANE_HEIGHT * scale;
    private final int gridCellSize = GRID_SIZE * scale;
    private boolean drawGrid = true;

    int minimapPlaneHalfWidth = minimapWidth / 2;
    int minimapPlaneHalfHeight = minimapHeight / 2;
    Vec2D minimapSize = Vec2D.of(minimapWidth, minimapHeight);
    Vec2D minimapOffset = Vec2D.of(MINIMAP_PROJECTION_X, MINIMAP_PROJECTION_Y);

    public MiniMap(Canvas canvas, GameState gameState) {
        super(Vec2D.of(INITIAL_CAMERA_POSITION_X, INITIAL_CAMERA_POSITION_Y));
        gc = canvas.getGraphicsContext2D();
        this.player = gameState.getPlayer();
        this.gameState = gameState;
    }

    @Override
    public void update(double deltaTimeMillis) {
        drawMiniMapBackground();
        drawMouseCoordinates();
        drawPlayerPosition();
        drawWalls();
        drawRays(gameState.getRays());
        drawGrid();
        drawFaceDirection();
        drawCenterPoint();
        drawPlayerRotationAngle();
    }

    private void drawRays(List<Ray> rays) {
        rays.forEach(ray -> {
            Vec2D originScene = worldToScene(ray.origin());
            Vec2D collisionScene = worldToScene(ray.collisionPoint());
            Vec2D collisionSceneScaled = originScene.add(originScene.subtract(collisionScene).multiply(-1).multiply(scale));

            double collisionX;
            double collisionY;

            Rectangle2D miniMapSceneBounds = getMiniMapSceneBounds();
            if (ray.status() == Ray.Status.INFINITE || !Rectangle2DUtil.containsPoint(miniMapSceneBounds, collisionSceneScaled)) {
                Vec2D intersectionWithMiniMapEdges = GridUtil.findCollisionPointAlongCellBorders(originScene, miniMapSceneBounds, ray);
                collisionX = intersectionWithMiniMapEdges.getX();
                collisionY = intersectionWithMiniMapEdges.getY();
            } else {
                collisionX = collisionSceneScaled.getX();
                collisionY = collisionSceneScaled.getY();
            }

            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeLine(originScene.getX(), originScene.getY(),
                    collisionX,
                    collisionY
            );
            gc.setLineWidth(1);

            gc.setFill(Color.CYAN);
            gc.fillOval((collisionX - 2), (collisionY - 2), 4, 4);
        });

    }

    private void drawMiniMapBackground() {
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(MINIMAP_PROJECTION_X, MINIMAP_PROJECTION_Y, minimapWidth, minimapHeight);
    }

    private void drawFaceDirection() {
        gc.setStroke(Color.BLUE);

        // the player is always centered in the minimap.
        // So x, y can just be the center of the minimap plus the minimap offset.
        Vec2D origin = minimapSize.multiply(0.5).add(minimapOffset);
        Vec2D direction = origin.add(player.rotation.multiply(20));

        gc.setLineWidth(2);
        gc.strokeLine(origin.getX(), origin.getY(), direction.getX(), direction.getY());
        gc.setLineWidth(1);
    }

    private void drawCenterPoint() {
        gc.setFill(Color.RED);

        int centerPointRadius = 4 * scale;
        double x = MINIMAP_PROJECTION_X + minimapPlaneHalfWidth - centerPointRadius;
        double y = MINIMAP_PROJECTION_Y + minimapPlaneHalfHeight - centerPointRadius;
        double diameter = centerPointRadius * 2;

        gc.fillOval(x, y, diameter, diameter);
    }

    private void drawGrid() {
        if (drawGrid) {
            gc.setLineWidth(1);
            gc.setStroke(GRID_LINE_COLOR);

            Vec2D scaledPosition = player.position.multiply(scale);
            double posX = scaledPosition.getX();
            double posY = scaledPosition.getY();

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

        gc.setFill(Color.WHITE);
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
        Vec2D playerPosition = player.getPosition();
        Vec2D playerScenePosition = worldToScene(playerPosition);
        gc.setFill(Color.WHITE);
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
        gc.setFill(Color.WHITE);
        gc.setFont(Fonts.TINY_NORMAL);
        gc.fillText(String.format("player rot angle rad:%.02f deg:%.02f", rotationAngle, Math.toDegrees(rotationAngle)),
                MINIMAP_PROJECTION_X,
                MINIMAP_PROJECTION_Y + minimapHeight + 75
        );
    }

    private Rectangle2D getMiniMapWorldBounds() {
        Vec2D scaledPosition = player.position.multiply(scale);
        int minWorldX = (int) (scaledPosition.getX()) - minimapPlaneHalfWidth;
        int minWorldY = (int) (scaledPosition.getY()) - minimapPlaneHalfHeight;
        return new Rectangle2D(minWorldX, minWorldY, minimapWidth, minimapHeight);
    }

    private Rectangle2D getMiniMapSceneBounds() {
        return new Rectangle2D(MINIMAP_PROJECTION_X, MINIMAP_PROJECTION_Y, minimapWidth, minimapHeight);
    }

    private void drawWalls() {
        Rectangle2D miniMapArea = getMiniMapWorldBounds();
        Vec2D scaledPosition = player.position.multiply(scale);
        gc.setFill(Color.DARKRED);
        Set<Wall> walls = gameState.getWalls();
        walls.forEach(wall -> {
            double worldX = wall.position.getX() * scale;
            double worldY = wall.position.getY() * scale;
            Rectangle2D wallArea = new Rectangle2D(worldX, worldY, gridCellSize, gridCellSize);
            Rectangle2D intersection = Rectangle2DUtil.getIntersection(miniMapArea, wallArea);
            if (intersection != null) {
                gc.fillRect(
                        intersection.getMinX() + minimapPlaneHalfWidth + MINIMAP_PROJECTION_X - scaledPosition.getX(),
                        intersection.getMinY() + minimapPlaneHalfHeight + MINIMAP_PROJECTION_Y - scaledPosition.getY(),
                        intersection.getWidth(),
                        intersection.getHeight()
                );
            }
        });
    }

    private Vec2D worldToScene(Vec2D world) {
        return world.subtract(player.position)
                .add(minimapSize.multiply(0.5))
                .add(minimapOffset);
    }

    private Vec2D sceneToWorld(Vec2D scene) {
        Vec2D position = player.position;
        return Vec2D.of(position.getX(), position.getY())
                .add(scene)
                .subtract(minimapSize.multiply(0.5).add(minimapOffset));
    }


}
