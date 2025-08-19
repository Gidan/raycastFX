package dev.gidan.raycastfx.util;

import javafx.geometry.Rectangle2D;

public class GridUtil {

    public static double nearestMultipleAbove(double n, double m) {
        if (m == 0) {
            return Integer.MAX_VALUE; // Or handle as appropriate
        }
        double remainder = ((n % m) + m) % m;
        if (remainder == 0) {
            return n; // If n is already a multiple, return the next one
        }
        return n + (m - remainder);
    }

    public static double nearestMultipleBelow(double n, double m) {
        if (m == 0) {
            return Integer.MIN_VALUE; // Or handle as appropriate
        }
        double remainder = ((n % m) + m) % m;
        if (remainder == 0) {
            return n; //If n is already a multiple, return the previous one.
        }
        return n - remainder;
    }

    public static Vec2D findCollisionPointAlongCellBorders(Vec2D origin, Rectangle2D cell, Ray ray) {
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

}
