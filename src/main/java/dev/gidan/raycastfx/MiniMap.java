package dev.gidan.raycastfx;

import dev.gidan.raycastfx.prefabs.Player;
import dev.gidan.raycastfx.prefabs.Wall;
import dev.gidan.raycastfx.util.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
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
    private static final int GRID_SIZE = 20;

    public static final double EPSILON = 0.03;

    private static final double FOV_ANGLE = 90;
    private static final int LINES = 120;

    private final int scale = 1;
    private final GraphicsContext gc;
    private final Player player;
    private final Set<Wall> walls;
    private final Canvas canvas;

    private final int minimapWidth = INITIAL_MINIMAP_PLANE_WIDTH * scale;
    private final int minimapHeight = INITIAL_MINIMAP_PLANE_HEIGHT * scale;
    private final int gridCellSize = GRID_SIZE * scale;
    private boolean drawGrid = true;

    int minimapPlaneHalfWidth = minimapWidth / 2;
    int minimapPlaneHalfHeight = minimapHeight / 2;
    Vec2D minimapSize = Vec2D.of(minimapWidth, minimapHeight);
    Vec2D minimapOffset = Vec2D.of(MINIMAP_PROJECTION_X, MINIMAP_PROJECTION_Y);

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

        drawCeilingAndFloor();

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        double halfCanvasHeight = canvasHeight / 2;
        double lineWidth = canvasWidth / LINES;
        double anglePerLine = FOV_ANGLE / LINES;
        double startAngle = (FOV_ANGLE / 2) * -1;

        double baseHue = Color.RED.getHue();
        double baseSaturation = Color.RED.getSaturation();

        List<Ray> rays = new ArrayList<>();

        for (int ray = 0; ray < LINES; ray++) {
            double angleDeltaInDegrees = startAngle + (anglePerLine * ray);
            Vec2D shootingDirection = player.rotation.rotateDeg(angleDeltaInDegrees);
            Ray rayResult = rayCast(new Ray(Ray.Status.SHOOTING, position, shootingDirection, position));
            rays.add(rayResult);
            double distance = rayResult.distance();
            if (distance < Ray.INFINITE_DISTANCE) {
                // adjust fish eye. better, but still not perfect. Now I get some sort of reversed fish eye along the edges of the screen.
                distance = Math.abs(Math.cos(Math.toRadians(angleDeltaInDegrees)) * distance);
                double x = ray * lineWidth;
                double lineHeight = (1 / Math.max(distance, 1)) * canvasHeight * 10;
                Color color = Color.hsb(baseHue, baseSaturation, Math.max(0, Math.min(1.0, 1 / distance * 10)));
                gc.setStroke(color);
                gc.setLineWidth(lineWidth + 2);
                double halfLineHeight = lineHeight / 2;
                gc.strokeLine(x, halfCanvasHeight - halfLineHeight, x, halfCanvasHeight + halfLineHeight);
            }
        }

        drawMiniMapBackground();
        drawMouseCoordinates();
        drawPlayerPosition();
        drawWalls();
        drawRays(rays);
        drawGrid();
        drawFaceDirection();
        drawCenterPoint();
        drawPlayerRotationAngle();
    }

    private void drawRays(List<Ray> rays) {
        rays.forEach(ray -> {
            Vec2D originScene = worldToScene(ray.origin());
            Vec2D collision = worldToScene(ray.collisionPoint());
            double collisionX;
            double collisionY;

            if (ray.status() == Ray.Status.INFINITE || !Rectangle2DUtil.containsPoint(getMiniMapSceneBounds(), collision)) {
                Vec2D intersectionWithMiniMapEdges = findCollisionPointAlongCellBorders(originScene, getMiniMapSceneBounds(), ray);
                collisionX = intersectionWithMiniMapEdges.getX();
                collisionY = intersectionWithMiniMapEdges.getY();
            } else {
                collisionX = collision.getX();
                collisionY = collision.getY();
            }

            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeLine(originScene.getX(), originScene.getY(),
                    collisionX,
                    collisionY
            );
            gc.setLineWidth(1);

            gc.setFill(Color.CYAN);
            gc.fillOval(collisionX - 2, collisionY - 2, 4, 4);
        });

    }

    private void drawMiniMapBackground() {
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(MINIMAP_PROJECTION_X, MINIMAP_PROJECTION_Y, minimapWidth, minimapHeight);
    }

    private void drawCeilingAndFloor() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        Color baseColor = Color.GRAY;

        int stripes = (int) ((canvasHeight / 2) / 5);
        for (int stripe = 0; stripe < stripes; stripe++) {
            Color ceilingColor = Color.hsb(baseColor.getHue(), baseColor.getSaturation(), (1.0 - (double) stripe / stripes) * 0.15);
            gc.setFill(ceilingColor);
            gc.fillRect(.0, stripe * 5, canvasWidth, 5);

            Color floorColor = Color.hsb(baseColor.getHue(), baseColor.getSaturation(), (1.0 - (double) stripe / stripes) * 0.25);
            gc.setFill(floorColor);
            gc.fillRect(.0, canvasHeight - (stripe * 5), canvasWidth, 5);
        }
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
        Vec2D playerPosition = this.position;
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
        int minWorldX = (int) (position.getX()) - minimapPlaneHalfWidth;
        int minWorldY = (int) (position.getY()) - minimapPlaneHalfHeight;
        return new Rectangle2D(minWorldX, minWorldY, minimapWidth, minimapHeight);
    }

    private Rectangle2D getMiniMapSceneBounds() {
        return new Rectangle2D(MINIMAP_PROJECTION_X, MINIMAP_PROJECTION_Y, minimapWidth, minimapHeight);
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

    private Ray rayCast(Ray ray) {
        Ray currentRay = ray;
        Ray resultRay = ray;

        while (currentRay.status() == Ray.Status.SHOOTING) {
            Vec2D origin = currentRay.origin();
            Vec2D direction = currentRay.direction();

            // if not colliding yet, but distance is greater than threshold, we'll assume that,
            // even if at some point it will collide with something,
            // we will not be able to see it.
            if (resultRay.distance() > Ray.INFINITE_DISTANCE) {
                resultRay = resultRay.withStatus(Ray.Status.INFINITE);
                break;
            }

            boolean isColliding = walls.stream()
                    // get the position of each wall
                    .map(w -> w.position.multiply(gridCellSize))
                    // create a 2d rect shape for each potential colliding wall
                    .map(wallPosition -> Rectangle2DUtil.rect(wallPosition, gridCellSize))
                    // filter out all the walls that are not intersecting the minimap bounds
                    //.filter(miniMapWorldBounds::intersects)
                    // check whether any one of those collide with the origin point
                    .anyMatch(wallBounds -> Rectangle2DUtil.containsPoint(wallBounds, origin));

            if (isColliding) {
                resultRay = resultRay.withStatus(Ray.Status.COLLIDING);
                break;
            }

            double originX = origin.getX();
            double originY = origin.getY();

            // find corner points of the grid cell the origin point is currently located
            double minX = GridUtil.nearestMultipleBelow(originX, gridCellSize);
            double minY = GridUtil.nearestMultipleBelow(originY, gridCellSize);
            double maxX = GridUtil.nearestMultipleAbove(originX, gridCellSize);
            double maxY = GridUtil.nearestMultipleAbove(originY, gridCellSize);

            Rectangle2D cell = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
            Vec2D collision = findCollisionPointAlongCellBorders(origin, cell, ray);

            // now extend the collision point by an epsilon to continue to the next grid cell
            currentRay = currentRay.withOrigin(collision.add(direction.multiply(EPSILON))).withCollisionPoint(collision);
            resultRay = ray.withStatus(currentRay.status()).withCollisionPoint(currentRay.collisionPoint());
        }

        return resultRay;
    }

    private Vec2D findCollisionPointAlongCellBorders(Vec2D origin, Rectangle2D cell, Ray ray) {
        double originX = origin.getX();
        double originY = origin.getY();
        Vec2D direction = ray.direction();

        double minX = cell.getMinX();
        double maxX = cell.getMaxX();
        double minY = cell.getMinY();
        double maxY = cell.getMaxY();

        Vec2D topLeft = Vec2D.of(minX, minY);
        Vec2D topRight = Vec2D.of(maxX, minY);
        Vec2D bottomRight = Vec2D.of(maxX, maxY);
        Vec2D bottomLeft = Vec2D.of(minX, maxY);

        // find the angles between the origin point and each corner point
        double topRightAngle = -(Math.PI - Vec2D.Util.angle(origin, topRight));
        double topLeftAngle = -(Math.PI - Vec2D.Util.angle(origin, topLeft));
        double bottomRightAngle = Math.PI + Vec2D.Util.angle(origin, bottomRight);
        double bottomLeftAngle = Math.PI + Vec2D.Util.angle(origin, bottomLeft);

        double rotationAngle = direction.angle();

        // now find the coordinates of the colliding point along the edge of the current grid cell
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

        return Vec2D.of(x, y);
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
