package dev.gidan.raycastfx.prefabs;

import dev.gidan.raycastfx.GameObject;
import dev.gidan.raycastfx.Input;
import dev.gidan.raycastfx.util.Vec2D;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class Player extends GameObject {

    public enum Status {
        IDLE, WALKING
    }

    /**
     * Player initial position intended as world position
     */
    private static final double INITIAL_PLAYER_POSITION_X = 0;
    private static final double INITIAL_PLAYER_POSITION_Y = 0;

    public static final Vec2D DEFAULT_POSITION = Vec2D.of(INITIAL_PLAYER_POSITION_X, INITIAL_PLAYER_POSITION_Y);

    private static final double MAX_SPEED = 20.0;

    /**
     * Player current speed
     */
    private double speed = 0;

    /**
     * Player current status
     */
    @Getter
    private Status status;

    @Getter
    private double distance;

    Vec2D previousDirection;

    public Player() {
        super(Vec2D.of(INITIAL_PLAYER_POSITION_X, INITIAL_PLAYER_POSITION_Y));
    }

    public Player(Vec2D position) {
        super(position);
    }

    @Override
    public void update(double deltaTimeMillis, final Function<Vec2D, Boolean> nextPositionCallback) {
        updateWorldPosition(deltaTimeMillis, nextPositionCallback);
        updateFaceDirection();
        updateStatus();
    }

    private void updateStatus() {
        status = speed > 0 ? Status.WALKING : Status.IDLE;
        if (status == Status.WALKING) {
            distance = (distance + speed * 0.2) % 360;
        }
    }

    private void updateWorldPosition(double deltaTime, final Function<Vec2D, Boolean> nextPositionCallback) {
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
            Vec2D nextPotentialPosition = position.add(previousDirection.multiply(deltaTime * speed));

            // if the next potential position is approved, assign it to actual position.
            // this is used to allow GameState to check collisions before the calculated position becomes valid even if it is potentially colliding with a wall.
            // simple solution, but if the player is touching a wall, it will stop there. The player movement is not modified by the colliding object.
            if (nextPositionCallback.apply(nextPotentialPosition)) {
                position = nextPotentialPosition;
            } else {
                speed = 0;
            }
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
