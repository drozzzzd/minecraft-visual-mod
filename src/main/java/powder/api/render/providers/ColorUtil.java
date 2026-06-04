package powder.api.render.providers;

import java.awt.Color;

public final class ColorUtil {

    public static int makeColor(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int alpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int multAlpha(int color, float factor) {
        int a = Math.max(0, Math.min(255, (int) (alpha(color) * factor)));
        return (a << 24) | (color & 0xFFFFFF);
    }

    public static int blend(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = (int) (alpha(c1) + (alpha(c2) - alpha(c1)) * t);
        int r = (int) (red(c1) + (red(c2) - red(c1)) * t);
        int g = (int) (green(c1) + (green(c2) - green(c1)) * t);
        int b = (int) (blue(c1) + (blue(c2) - blue(c1)) * t);
        return makeColor(r, g, b, a);
    }

    /** Moving hue color used by trail effects. */
    public static int fade(int index) {
        float hue = ((index % 360) + 360) % 360 / 360f;
        Color c = Color.getHSBColor(hue, 1f, 1f);
        return makeColor(c.getRed(), c.getGreen(), c.getBlue(), 255);
    }

}
