package dev.gidan.raycastfx;

import dev.gidan.raycastfx.prefabs.Player;
import dev.gidan.raycastfx.prefabs.Wall;
import dev.gidan.raycastfx.util.Ray;
import dev.gidan.raycastfx.util.Vec2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GameState {

    @Getter
    private final Player player;

    @Getter
    private final Set<Wall> walls;

    @Getter
    @Setter
    private List<Ray> rays;

    public GameState() {
        player = new Player(loadPlayer());
        walls = loadMap();
    }

    private Vec2D loadPlayer() {
        Image image = new Image(Objects.requireNonNull(getClass().getResource("/map.png")).toExternalForm());

        PixelReader pixelReader = image.getPixelReader();
        double width = image.getWidth();
        double height = image.getHeight();

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Color color = pixelReader.getColor(c, r);
                if (color.equals(Color.RED)) {
                    return Vec2D.of(r,c).multiply(MiniMap.GRID_SIZE);
                }
            }
        }

        return Player.DEFAULT_POSITION;
    }

    private Set<Wall> loadMap() {
        Image image = new Image(Objects.requireNonNull(getClass().getResource("/map.png")).toExternalForm());

        PixelReader pixelReader = image.getPixelReader();
        double width = image.getWidth();
        double height = image.getHeight();

        Set<Wall> walls = new HashSet<>();

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Color color = pixelReader.getColor(c, r);
                if (color.equals(Color.BLACK)) {
                    walls.add(new Wall(Vec2D.of(c, r).multiply(MiniMap.GRID_SIZE)));
                }
            }
        }

        return walls;
    }


}
