package dev.gidan.raycastfx.util;

public class GridUtil {

    public static double nearestMultipleAbove(double n, double m) {
        if (m == 0) {
            return Integer.MAX_VALUE; // Or handle as appropriate
        }
        double remainder = ((n % m) + m) % m;
        if (remainder == 0) {
            return n; // If n is already a multiple, return the next one
        }
        return n + (m - remainder);
    }

    public static double nearestMultipleBelow(double n, double m) {
        if (m == 0) {
            return Integer.MIN_VALUE; // Or handle as appropriate
        }
        double remainder = ((n % m) + m) % m;
        if (remainder == 0) {
            return n; //If n is already a multiple, return the previous one.
        }
        return n - remainder;
    }
}
