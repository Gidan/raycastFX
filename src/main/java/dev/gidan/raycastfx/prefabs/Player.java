package dev.gidan.raycastfx.prefabs;

import dev.gidan.raycastfx.GameObject;
import dev.gidan.raycastfx.Input;
import dev.gidan.raycastfx.util.Vec2D;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Player extends GameObject {

    public enum Status {
        IDLE, WALKING
    }

    private static final double INITIAL_PLAYER_POSITION_X = 0;
    private static final double INITIAL_PLAYER_POSITION_Y = 0;

    public static final Vec2D DEFAULT_POSITION = Vec2D.of(INITIAL_PLAYER_POSITION_X, INITIAL_PLAYER_POSITION_Y);

    private static final double MAX_SPEED = 20.0;
    private double speed = 0;

    @Getter
    private Status status;

    @Getter
    private double distance;

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
        updateStatus();
    }

    private void updateStatus() {
        status = speed > 0 ? Status.WALKING : Status.IDLE;
        if (status == Status.WALKING) {
            distance = (distance + speed * 0.2) % 360;
        }
    }

    Vec2D previousDirection;
    private void updateWorldPosition(double deltaTime) {
        Input input = Input.getInstance();
        Vec2D direction = Vec2D.ZERO;

        if (input.isMovingForward()) {
            direction = direction.add(rotation);
        }
        if (input.isMovingBackward()) {
            direction = direction.add(rotation.rotate(Math.PI));
        }
        if (input.isStrafeLeft()) {
            direction = direction.add(rotation.rotate(Math.PI / 2 * -1));
        }
        if (input.isStrafeRight()) {
            direction = direction.add(rotation.rotate(Math.PI / 2));
        }

        if (direction.magnitude() > 0) {
            previousDirection = direction;
            speed = Math.min(speed + 0.3, MAX_SPEED);
        } else {
            speed = Math.max(speed - 0.4, 0.0);
        }

        if (speed > 0) {
            position = position.add(previousDirection.multiply(deltaTime * speed));
        }
    }

    private void updateFaceDirection() {
        Input input = Input.getInstance();

        double angle = Math.toRadians(1);

        if (input.isTurningRight()) {
            rotation = rotation.rotate(angle);
        }
        if (input.isTurningLeft()) {
            rotation = rotation.rotate(-angle);
        }
    }


}
