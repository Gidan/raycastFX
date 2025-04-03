package dev.gidan.raycastfx.prefabs;

import dev.gidan.raycastfx.GameObject;
import dev.gidan.raycastfx.util.Vec2D;
import javafx.geometry.Rectangle2D;

public class Wall extends GameObject {

    public static Wall at(double x, double y) {
        return new Wall(Vec2D.of(x, y));
    }

    public Wall(Vec2D position) {
        super(position);
    }

}
