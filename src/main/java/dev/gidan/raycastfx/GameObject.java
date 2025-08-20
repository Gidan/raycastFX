package dev.gidan.raycastfx;

import dev.gidan.raycastfx.util.Vec2D;
import lombok.Getter;

import java.util.function.Function;

@Getter
public class GameObject {

    /**
     * GameObject's world position
     */
    protected Vec2D position;
    protected Vec2D rotation;

    protected GameObject(Vec2D position) {
        this.position = position;
        rotation = Vec2D.UP;
    }

    public void update(double deltaTimeInMillis, final Function<Vec2D, Boolean> nextPositionCallback) {

    }


}
