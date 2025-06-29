package dev.gidan.raycastfx.util;

import lombok.Getter;

import java.util.Objects;

public class Vec2D {
    @Getter
    private double x;
    @Getter
    private double y;

    public static Vec2D of(double x, double y) {
        return new Vec2D(x, y);
    }

    // Constructor
    private Vec2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Addition
    public Vec2D add(Vec2D other) {
        return new Vec2D(this.x + other.x, this.y + other.y);
    }

    // Subtraction
    public Vec2D subtract(Vec2D other) {
        return new Vec2D(this.x - other.x, this.y - other.y);
    }

    // Scalar multiplication
    public Vec2D multiply(double scalar) {
        return new Vec2D(this.x * scalar, this.y * scalar);
    }

    public Vec2D rotateDeg(double angleDegrees) {
        return rotate(Math.toRadians(angleDegrees));
    }

    public Vec2D rotate(double angleRadians) {
        double cosTheta = Math.cos(angleRadians);
        double sinTheta = Math.sin(angleRadians);

        double newX = x * cosTheta - y * sinTheta;
        double newY = x * sinTheta + y * cosTheta;

        return new Vec2D(newX, newY);
    }

    public Vec2D rotateAroundPoint(Vec2D rotationPoint, double angleRadians) {
        // Translate the vector so that the rotation point is the origin
        Vec2D translatedVector = new Vec2D(x - rotationPoint.x, y - rotationPoint.y);

        // Perform the rotation
        Vec2D rotatedTranslatedVector = translatedVector.rotate(angleRadians);

        // Translate the vector back
        return new Vec2D(rotatedTranslatedVector.x + rotationPoint.x, rotatedTranslatedVector.y + rotationPoint.y);
    }

    // Dot product
    public double dot(Vec2D other) {
        return this.x * other.x + this.y * other.y;
    }

    // Magnitude
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    // Normalize
    public Vec2D normalize() {
        double mag = magnitude();
        return new Vec2D(x / mag, y / mag);
    }

    public double angle() {
        return Math.atan2(y, x);
    }

    // String representation
    @Override
    public String toString() {
        return "Vec2D(" + x + ", " + y + ")";
    }

    // Equals method
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vec2D Vec2D = (Vec2D) obj;
        return Double.compare(Vec2D.x, x) == 0 && Double.compare(Vec2D.y, y) == 0;
    }

    // Hash code method
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public static Vec2D ZERO = new Vec2D(0, 0);
    public static Vec2D UP = new Vec2D(0, -1);
    public static Vec2D LEFT = new Vec2D(-1, 0);
    public static Vec2D RIGHT = new Vec2D(1, 0);
    public static Vec2D DOWN = new Vec2D(0, 1);

    public static final class Util {

        public static double angle(Vec2D v1, Vec2D v2) {
            double dx = v1.x - v2.x;
            double dy = v1.y - v2.y;
            return Math.atan2(dy, dx);
        }

    }
}

