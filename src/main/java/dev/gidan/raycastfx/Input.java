package dev.gidan.raycastfx;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Input {

    private static final Logger LOGGER = LoggerFactory.getLogger(Input.class);

    private final Map<KeyCode, Integer> states = new HashMap<>();
    private double mouseX;
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
            LOGGER.trace("key pressed: {}", key);
            states.put(keyCode, 1);
        });

        scene.addEventHandler(KeyEvent.KEY_RELEASED, (key) -> {
            KeyCode keyCode = key.getCode();
            LOGGER.trace("key released: {}", key);
            states.put(keyCode, 0);
        });

        scene.setOnMouseMoved(mouseEvent -> {
            mouseX = mouseEvent.getSceneX();
            mouseY = mouseEvent.getSceneY();
        });

        scene.getRoot().requestFocus();
    }

    public boolean isPressed(KeyCode keyCode) {
        return states.containsKey(keyCode) && states.get(keyCode) == 1;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }
}
