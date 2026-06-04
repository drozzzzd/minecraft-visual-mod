package powder.api.render.providers;

public class ThemeColor {

    private final int argb;

    public ThemeColor(int argb) {
        this.argb = argb;
    }

    public int getRGB() {
        return argb;
    }

    public ThemeColor withAlpha(int alpha) {
        return new ThemeColor(((alpha & 0xFF) << 24) | (argb & 0xFFFFFF));
    }

}
