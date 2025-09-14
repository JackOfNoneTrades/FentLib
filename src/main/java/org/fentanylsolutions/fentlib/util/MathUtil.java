package org.fentanylsolutions.fentlib.util;

public class MathUtil {

    /* from Minecraft 1.19 net.minecraft.util.Mth */
    public static double lerp(double t, double start, double end) {
        return start + t * (end - start);
    }

    /* from Minecraft 1.19 net.minecraft.util.Mth */
    public static double clampedLerp(double start, double end, double t) {
        if (t < 0.0D) {
            return start;
        } else {
            return t > 1.0D ? end : lerp(t, start, end);
        }
    }

    /* from Minecraft 1.19 net.minecraft.util.Mth */
    public static float clampedLerp(float start, float end, float t) {
        if (t < 0.0F) {
            return start;
        } else {
            return t > 1.0F ? end : (float) lerp(t, start, end);
        }
    }
}
