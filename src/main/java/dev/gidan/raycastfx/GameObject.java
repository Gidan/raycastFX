package dev.gidan.raycastfx;

import dev.gidan.raycastfx.util.Vec2D;

public class GameObject {

    protected Vec2D position;
    protected Vec2D faceDirection;

    protected GameObject(Vec2D position) {
        this.position = position;
        faceDirection = Vec2D.UP;
    }

    public void update(double deltaTimeInMillis) {

    }

    public Vec2D getPosition() {
        return position;
    }

    public Vec2D getFaceDirection() {
        return faceDirection;
    }

}
