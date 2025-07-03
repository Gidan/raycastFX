package dev.gidan.raycastfx;

import dev.gidan.raycastfx.util.Vec2D;
import lombok.Getter;

@Getter
public class GameObject {

    protected Vec2D position;
    protected Vec2D rotation;

    protected GameObject(Vec2D position) {
        this.position = position;
        rotation = Vec2D.UP;
    }

    public void update(double deltaTimeInMillis) {

    }


}
