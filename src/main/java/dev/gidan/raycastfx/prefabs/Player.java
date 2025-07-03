package dev.gidan.raycastfx.prefabs;

import dev.gidan.raycastfx.GameObject;
import dev.gidan.raycastfx.Input;
import dev.gidan.raycastfx.util.Vec2D;
import javafx.scene.input.KeyCode;

public class Player extends GameObject {

    private static final double INITIAL_PLAYER_POSITION_X = 0;
    private static final double INITIAL_PLAYER_POSITION_Y = 0;

    public static final Vec2D DEFAULT_POSITION = Vec2D.of(INITIAL_PLAYER_POSITION_X, INITIAL_PLAYER_POSITION_Y);

    private double speed = 20.0;

    public Player() {
        super(Vec2D.of(INITIAL_PLAYER_POSITION_X, INITIAL_PLAYER_POSITION_Y));
    }

    public Player(Vec2D position) {
        super(position);
    }

    @Override
    public void update(double deltaTimeMillis) {
        updateWorldPosition(deltaTimeMillis);
        updateFaceDirection();
    }

    private void updateWorldPosition(double deltaTime) {
        Input input = Input.getInstance();
        Vec2D direction = Vec2D.ZERO;
        if (input.isPressed(KeyCode.W)) {
            direction = direction.add(rotation);
        }
        if (input.isPressed(KeyCode.S)) {
            direction = direction.add(rotation.rotate(Math.PI));
        }
        if (input.isPressed(KeyCode.A)) {
            direction = direction.add(rotation.rotate(Math.PI / 2 * -1));
        }
        if (input.isPressed(KeyCode.D)) {
            direction = direction.add(rotation.rotate(Math.PI / 2));
        }

        if (direction.magnitude() > 0) {
            position = position.add(direction.multiply(deltaTime * speed));
        }
    }

    private void updateFaceDirection() {
        Input input = Input.getInstance();

        double angle = Math.toRadians(1);

        if (input.isPressed(KeyCode.E)) {
            rotation = rotation.rotate(angle);
        }
        if (input.isPressed(KeyCode.Q)) {
            rotation = rotation.rotate(-angle);
        }
    }


}
