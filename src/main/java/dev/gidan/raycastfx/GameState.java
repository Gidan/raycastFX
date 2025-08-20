package dev.gidan.raycastfx;

import dev.gidan.raycastfx.prefabs.Player;
import dev.gidan.raycastfx.prefabs.Wall;
import dev.gidan.raycastfx.util.Ray;
import dev.gidan.raycastfx.util.Vec2D;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class GameState {

    @Getter
    private final Player player;

    @Getter
    private final Set<Wall> walls;

    @Getter
    @Setter
    private List<Ray> rays;

    public GameState() {
        player = new Player(loadPlayerInitialPosition());
        walls = loadWalls();
    }

    public void update(double deltaTimeMillis) {
        player.update(deltaTimeMillis, nextPosition -> {
            final Point2D point2D = nextPosition.toPoint2D();
            return walls.stream().map(Wall::area).noneMatch(wallArea -> wallArea.contains(point2D));
        });
    }

    /**
     * Reads the initial player position from the external map image.
     *
     * @return the player position as Vec2D object
     */
    private Vec2D loadPlayerInitialPosition() {
        AtomicReference<Vec2D> playerInitialPosition = new AtomicReference<>(Player.DEFAULT_POSITION);

        loadMap((color, point) -> {
            if (color.equals(Color.RED)) {
                playerInitialPosition.set(point.multiply(MiniMap.GRID_SIZE).add(Vec2D.of(MiniMap.GRID_SIZE).half()));
                return false;
            }
            return true;
        });

        return playerInitialPosition.get();
    }

    /**
     * Reads the external map image to load the walls positions.
     *
     * @return a set of Wall instances representing the non-walkable sections of the map.
     */
    private Set<Wall> loadWalls() {
        Set<Wall> walls = new HashSet<>();

        loadMap((color, point) -> {
            if (color.equals(Color.BLACK)) {
                walls.add(new Wall(point.multiply(MiniMap.GRID_SIZE)));
            }
            return true;
        });

        return walls;
    }

    private void loadMap(BiFunction<Color, Vec2D, Boolean> pixelCallback) {
        Image image = new Image(Objects.requireNonNull(getClass().getResource("/map.png")).toExternalForm());

        PixelReader pixelReader = image.getPixelReader();
        double width = image.getWidth();
        double height = image.getHeight();

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Color color = pixelReader.getColor(c, r);
                Boolean shouldContinue = pixelCallback.apply(color, Vec2D.of(c, r));
                if (!shouldContinue) return;
            }
        }
    }


}
