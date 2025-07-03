package dev.gidan.raycastfx.util;

import lombok.With;

public record Ray(@With Status status, @With Vec2D origin, Vec2D direction, @With Vec2D collisionPoint) {

    public static final double INFINITE_DISTANCE = 200;

    public enum Status {
        SHOOTING, COLLIDING, INFINITE
    }

    public double distance() {
        if (status == Status.INFINITE) return INFINITE_DISTANCE;
        return Math.min(Math.abs(origin.subtract(collisionPoint).magnitude()), INFINITE_DISTANCE);
    }


}
