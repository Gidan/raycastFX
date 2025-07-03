package dev.gidan.raycastfx;

import dev.gidan.raycastfx.util.Vec2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class Input {

    private final Map<KeyCode, Integer> states = new HashMap<>();

    @Getter
    private double mouseX;
    @Getter
    private double mouseY;

    private static Input instance;

    public static Input getInstance() {
        if (instance == null) {
            instance = new Input();
        }
        return instance;
    }

    public void init(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            KeyCode keyCode = key.getCode();
            log.trace("key pressed: {}", key);
            states.put(keyCode, 1);
        });

        scene.addEventHandler(KeyEvent.KEY_RELEASED, (key) -> {
            KeyCode keyCode = key.getCode();
            log.trace("key released: {}", key);
            states.put(keyCode, 0);
        });

        scene.setOnMouseMoved(mouseEvent -> {
            mouseX = mouseEvent.getSceneX();
            mouseY = mouseEvent.getSceneY();
        });

        scene.getRoot().requestFocus();
    }

    private boolean isPressed(KeyCode keyCode) {
        return states.containsKey(keyCode) && states.get(keyCode) == 1;
    }

    public boolean isMovingForward() {
        return isPressed(KeyCode.W);
    }

    public boolean isMovingBackward() {
        return isPressed(KeyCode.S);
    }

    public boolean isStrafeRight() {
        return isPressed(KeyCode.D);
    }

    public boolean isStrafeLeft() {
        return isPressed(KeyCode.A);
    }

    public boolean isTurningRight() {
        return isPressed(KeyCode.E);
    }

    public boolean isTurningLeft() {
        return isPressed(KeyCode.Q);
    }

    public boolean isMoving() {
        return isMovingBackward() || isMovingForward() || isStrafeRight() || isStrafeLeft();
    }

    public Vec2D mouse() {
        return Vec2D.of(mouseX, mouseY);
    }
}
