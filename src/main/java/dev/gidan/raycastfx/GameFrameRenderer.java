package dev.gidan.raycastfx;

import dev.gidan.raycastfx.prefabs.Player;
import dev.gidan.raycastfx.util.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class GameFrameRenderer {

    private static final double WALK_MAX_Y_OFFSET = 5.0;
    private static final int LINES = 120;
    private static final double FOV_ANGLE = 90;

    private final Canvas canvas;
    private final Player player;
    private final GameState gameState;

    private double yOffset = 0;

    public GameFrameRenderer(Canvas canvas, GameState gameState) {
        this.canvas = canvas;
        this.player = gameState.getPlayer();
        this.gameState = gameState;
    }

    public void update(double deltaTimeMillis) {
        if (player.getStatus() == Player.Status.WALKING) {
            yOffset = Math.sin(Math.toRadians(player.getDistance())) * WALK_MAX_Y_OFFSET;
        }

        drawCeilingAndFloor();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        double halfCanvasHeight = canvasHeight / 2;
        double lineWidth = canvasWidth / LINES;
        double anglePerLine = FOV_ANGLE / (LINES - 1);
        double startAngle = (FOV_ANGLE / 2) * -1;

        double baseHue = Color.RED.getHue();
        double baseSaturation = Color.RED.getSaturation();

        List<Ray> rays = new ArrayList<>();

        for (int ray = 0; ray < LINES; ray++) {
            double angleDeltaInDegrees = startAngle + (anglePerLine * ray);
            Vec2D shootingDirection = player.rotation.rotateDeg(angleDeltaInDegrees);
            Vec2D playerPosition = player.getPosition();
            Ray rayResult = rayCast(new Ray(Ray.Status.SHOOTING, playerPosition, shootingDirection));
            rays.add(rayResult);
            double distance = rayResult.distance();
            if (distance < Ray.INFINITE_DISTANCE) {
                // adjust fish eye. better, but still not perfect. Now I get some sort of reversed fish eye along the edges of the screen.
                distance = Math.abs(Math.cos(Math.toRadians(angleDeltaInDegrees)) * distance);
                double x = ray * lineWidth;
                double lineHeight = (1 / Math.max(distance, 1)) * canvasHeight * 10;
                Color color = Color.hsb(baseHue, baseSaturation, Math.max(0, Math.min(1.0, 1 / distance * 10)));
                double halfLineHeight = lineHeight / 2;
                gc.setFill(color);

                //adjust x and lineWidth to overlap lines a bit to avoid gaps between lines
                double xOverlap = x - 1;
                double lineWidthOverlap = lineWidth + 2;

                gc.fillRect(xOverlap, halfCanvasHeight - halfLineHeight + yOffset, lineWidthOverlap, lineHeight);
            }
        }

        gameState.setRays(rays);
    }

    private void drawCeilingAndFloor() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Color baseColor = Color.GRAY;
        double baseHue = baseColor.getHue();
        double baseSaturation = baseColor.getSaturation();

        int stripeHeight = 5;

        double ceilingHeight = canvasHeight / 2 + yOffset;
        double floorHeight = canvasHeight - ceilingHeight;
        int ceilingStripes = (int) (ceilingHeight / stripeHeight);
        int floorStripes = (int) (floorHeight / stripeHeight);
        for (int stripe = 0; stripe < ceilingStripes; stripe++) {
            Color ceilingColor = Color.hsb(baseHue, baseSaturation, (1.0 - (double) stripe / ceilingStripes) * 0.15);
            gc.setFill(ceilingColor);
            gc.fillRect(.0, stripe * 5, canvasWidth, 5);
        }

        for (int stripe = 0; stripe < floorStripes; stripe++) {
            Color floorColor = Color.hsb(baseHue, baseSaturation, (1.0 - (double) stripe / floorStripes) * 0.25);
            gc.setFill(floorColor);
            gc.fillRect(.0, canvasHeight - (stripe * 5), canvasWidth, 5);
        }
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
            if (resultRay.distance() >= Ray.INFINITE_DISTANCE) {
                resultRay = resultRay.withStatus(Ray.Status.INFINITE);
                break;
            }

            boolean isColliding = gameState.getWalls().stream()
                    // get the position of each wall
                    .map(w -> w.position)
                    // create a 2d rect shape for each potential colliding wall
                    .map(wallPosition -> Rectangle2DUtil.rect(wallPosition, MiniMap.GRID_SIZE))
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
            double minX = GridUtil.nearestMultipleBelow(originX, MiniMap.GRID_SIZE);
            double minY = GridUtil.nearestMultipleBelow(originY, MiniMap.GRID_SIZE);
            double maxX = GridUtil.nearestMultipleAbove(originX, MiniMap.GRID_SIZE);
            double maxY = GridUtil.nearestMultipleAbove(originY, MiniMap.GRID_SIZE);

            Rectangle2D cell = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
            Vec2D collision = GridUtil.findCollisionPointAlongCellBorders(origin, cell, ray);

            // now extend the origin point by an epsilon to continue to the next grid cell
            currentRay = currentRay.withOrigin(collision.add(direction.multiply(MiniMap.EPSILON))).withCollisionPoint(collision);
            resultRay = ray.withStatus(currentRay.status()).withCollisionPoint(currentRay.collisionPoint());
        }

        return resultRay;
    }





}
