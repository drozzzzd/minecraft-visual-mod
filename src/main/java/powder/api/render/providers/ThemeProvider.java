package powder.api.render.providers;

import java.awt.Color;

/**
 * Torov Visual theme colors. Index is treated as a hue angle (0..360),
 * producing a smooth animated client gradient.
 */
public final class ThemeProvider {

    public static ThemeColor getClientColor(int index) {
        float hue = (((index % 360) + 360) % 360) / 360f;
        Color c = Color.getHSBColor(hue, 0.6f, 1f);
        return new ThemeColor(0xFF000000 | (c.getRGB() & 0xFFFFFF));
    }

}
