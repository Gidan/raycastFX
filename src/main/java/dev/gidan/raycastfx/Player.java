package dev.gidan.raycastfx;

import dev.gidan.raycastfx.util.Vec2D;
import javafx.scene.input.KeyCode;

public class Player extends GameObject {

    private static final double INITIAL_PLAYER_POSITION_X = 0;
    private static final double INITIAL_PLAYER_POSITION_Y = 0;

    private int speed = 20;

    public Player() {
        super(new Vec2D(INITIAL_PLAYER_POSITION_X, INITIAL_PLAYER_POSITION_Y));
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
            direction = direction.add(Vec2D.UP);
        }
        if (input.isPressed(KeyCode.S)) {
            direction = direction.add(Vec2D.DOWN);
        }
        if (input.isPressed(KeyCode.A)) {
            direction = direction.add(Vec2D.LEFT);
        }
        if (input.isPressed(KeyCode.D)) {
            direction = direction.add(Vec2D.RIGHT);
        }

        if (direction.magnitude() > 0) {
            position = position.add(direction.multiply(deltaTime * speed));
        }
    }

    private void updateFaceDirection() {
        Input input = Input.getInstance();

        double angle = Math.toRadians(1);

        if (input.isPressed(KeyCode.E)) {
            faceDirection = faceDirection.rotate(angle);
        }
        if (input.isPressed(KeyCode.Q)) {
            faceDirection = faceDirection.rotate(-angle);
        }
    }


}
