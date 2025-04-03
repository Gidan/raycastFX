package dev.gidan.raycastfx.util;

public class FPSCount {

    private static final int FRAMES_TO_SKIP = 60;

    private int frameCount;
    private int fps;

    public int frame(double deltaInSeconds) {
        frameCount++;
        if (frameCount >= FRAMES_TO_SKIP) {
            frameCount = 0;
            fps = (int) (1.0 / deltaInSeconds);
        }

        return fps;
    }


}
