package dev.gidan.raycastfx;

import dev.gidan.raycastfx.util.GridUtil;
import dev.gidan.raycastfx.util.Vec2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoordinatesTest {

    @Test
    public void coordinatesTest() {
        double gridCellSize = 40.0;

        {
            Vec2D origin = Vec2D.of(0, 0);
            assertEquals(Vec2D.of(0, 0), topLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(0, 0), topRight(origin, gridCellSize));
            assertEquals(Vec2D.of(0, 0), bottomLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(0, 0), bottomRight(origin, gridCellSize));
        }

        {
            Vec2D origin = Vec2D.of(10, 0);
            assertEquals(Vec2D.of(0, 0), topLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(40, 0), topRight(origin, gridCellSize));
            assertEquals(Vec2D.of(0, 0), bottomLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(40, 0), bottomRight(origin, gridCellSize));
        }

        {
            Vec2D origin = Vec2D.of(10, 10);
            assertEquals(Vec2D.of(0, 0), topLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(40, 0), topRight(origin, gridCellSize));
            assertEquals(Vec2D.of(0, 40), bottomLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(40, 40), bottomRight(origin, gridCellSize));
        }

        {
            Vec2D origin = Vec2D.of(6, 6);
            assertEquals(Vec2D.of(0, 0), topLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(40, 0), topRight(origin, gridCellSize));
            assertEquals(Vec2D.of(0, 40), bottomLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(40, 40), bottomRight(origin, gridCellSize));
        }

        {
            Vec2D origin = Vec2D.of(6, -6);
            assertEquals(Vec2D.of(0, -40), topLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(40, -40), topRight(origin, gridCellSize));
            assertEquals(Vec2D.of(0, 0), bottomLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(40, 0), bottomRight(origin, gridCellSize));
        }

        {
            Vec2D origin = Vec2D.of(-6, -6);
            assertEquals(Vec2D.of(-40, -40), topLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(0, -40), topRight(origin, gridCellSize));
            assertEquals(Vec2D.of(-40, 0), bottomLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(0, 0), bottomRight(origin, gridCellSize));
        }

        {
            Vec2D origin = Vec2D.of(46, 10);
            assertEquals(Vec2D.of(40, 0), topLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(80, 0), topRight(origin, gridCellSize));
            assertEquals(Vec2D.of(40, 40), bottomLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(80, 40), bottomRight(origin, gridCellSize));
        }

        {
            Vec2D origin = Vec2D.of(-46, 10);
            assertEquals(Vec2D.of(-80, 0), topLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(-40, 0), topRight(origin, gridCellSize));
            assertEquals(Vec2D.of(-80, 40), bottomLeft(origin, gridCellSize));
            assertEquals(Vec2D.of(-40, 40), bottomRight(origin, gridCellSize));
        }


    }

    private Vec2D topLeft(Vec2D origin, double cellSize) {
        double x = GridUtil.nearestMultipleBelow(origin.getX(), cellSize);
        double y = GridUtil.nearestMultipleBelow(origin.getY(), cellSize);
        return Vec2D.of(x, y);
    }

    private Vec2D topRight(Vec2D origin, double cellSize) {
        double x = GridUtil.nearestMultipleAbove(origin.getX(), cellSize);
        double y = GridUtil.nearestMultipleBelow(origin.getY(), cellSize);
        return Vec2D.of(x, y);
    }

    private Vec2D bottomLeft(Vec2D origin, double cellSize) {
        double x = GridUtil.nearestMultipleBelow(origin.getX(), cellSize);
        double y = GridUtil.nearestMultipleAbove(origin.getY(), cellSize);
        return Vec2D.of(x, y);
    }

    private Vec2D bottomRight(Vec2D origin, double cellSize) {
        double x = GridUtil.nearestMultipleAbove(origin.getX(), cellSize);
        double y = GridUtil.nearestMultipleAbove(origin.getY(), cellSize);
        return Vec2D.of(x, y);
    }


}
