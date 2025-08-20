package dev.gidan.raycastfx.prefabs;

import dev.gidan.raycastfx.GameObject;
import dev.gidan.raycastfx.MiniMap;
import dev.gidan.raycastfx.util.Vec2D;
import javafx.geometry.Rectangle2D;

public class Wall extends GameObject {

    private Rectangle2D area;

    public Wall(Vec2D position) {
        super(position);
    }

    public Rectangle2D area() {
        if (area == null) {
            area = new Rectangle2D(position.getX(), position.getY(), MiniMap.GRID_SIZE, MiniMap.GRID_SIZE);
        }
        return area;
    }

}
