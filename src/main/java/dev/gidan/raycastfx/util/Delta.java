package dev.gidan.raycastfx.util;

public class Delta {

    long start = 0L;

    public double seconds(long nowInNanoSeconds) {
        long nowInMillis = nowInNanoSeconds / 1_000_000;

        if (start == 0L) {
            start = nowInMillis;
        }

        double delta = (nowInMillis - start) / 1000.0;
        start = nowInMillis;

        return delta;
    }

}
