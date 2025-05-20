package dev.gidan.raycastfx.util;

import javafx.geometry.Rectangle2D;

public class Rectangle2DUtil {

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

    public static boolean containsPoint(Rectangle2D rect, Vec2D point) {
        return point.getX() >= rect.getMinX() && point.getX() <= rect.getMaxX() &&
                point.getY() >= rect.getMinY() && point.getY() <= rect.getMaxY();
    }

    public static Rectangle2D rect(Vec2D pos, double size) {
        return new Rectangle2D(pos.getX(), pos.getY(), size, size);
    }



}
