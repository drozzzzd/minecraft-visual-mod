package torovvisual.api.system.font;

import net.minecraft.client.util.math.MatrixStack;
import powder.api.render.drawing.builders.Builder;
import powder.api.render.msdf.MsdfFont;

/**
 * MSDF font facade mirroring the Rich-Modern {@code Fonts}/{@code Font} API so its
 * ClickGUI / HUD renderers can be ported almost verbatim. The atlases (the very
 * same {@code .json}/{@code .png} files shipped with Rich-Modern) live under
 * {@code assets/mre/fonts/} and are rendered through Powder's existing
 * {@code msdf_font} shader (see {@link powder.api.render.drawing.renderers.impl.BuiltText}).
 *
 * <p>The current matrix is supplied per frame via {@link #begin(MatrixStack)} so
 * call sites keep the Rich-Modern signature {@code FONT.draw(text, x, y, size, color)}.
 */
public final class RichFonts {

    public static final RichFont BOLD = new RichFont("bold");
    public static final RichFont REGULAR = new RichFont("regular");
    public static final RichFont DEFAULT = new RichFont("default");
    public static final RichFont ICONS = new RichFont("icons");
    public static final RichFont GUI_ICONS = new RichFont("guiicons");
    public static final RichFont HUD_ICONS = new RichFont("hudicons");
    public static final RichFont CATEGORY_ICONS = new RichFont("categoryicons");
    public static final RichFont MAINMENU_ICONS = new RichFont("mainmenuicons");

    private static MatrixStack matrix;

    private RichFonts() {}

    /** Set the matrix every text call should be transformed by (call once per frame). */
    public static void begin(MatrixStack stack) {
        matrix = stack;
    }

    public static final class RichFont {
        private final String name;
        private MsdfFont font;
        private boolean failed;

        private RichFont(String name) {
            this.name = name;
        }

        private MsdfFont font() {
            if (font == null && !failed) {
                try {
                    font = MsdfFont.builder().name(name).data(name).atlas(name).build();
                } catch (Throwable t) {
                    failed = true;
                }
            }
            return font;
        }

        public void draw(String text, float x, float y, float size, int color) {
            drawOutlined(text, x, y, size, color, 0, 0f);
        }

        public void drawCentered(String text, float x, float y, float size, int color) {
            draw(text, x - getWidth(text, size) / 2f, y, size, color);
        }

        /** Draws text with an MSDF outline/glow (outlineThickness in em, e.g. 0.2–0.4). */
        public void drawOutlined(String text, float x, float y, float size, int color, int outlineColor, float outlineThickness) {
            if (matrix == null || text == null || text.isEmpty()) return;
            MsdfFont f = font();
            if (f == null) return;
            var builder = Builder.text().font(f).text(text).color(color).size(size).thickness(0.05f);
            if (outlineThickness > 0f) builder = builder.outline(outlineColor, outlineThickness);
            builder.build().render(matrix.peek().getPositionMatrix(), x, y);
        }

        public void drawCenteredOutlined(String text, float x, float y, float size, int color, int outlineColor, float outlineThickness) {
            drawOutlined(text, x - getWidth(text, size) / 2f, y, size, color, outlineColor, outlineThickness);
        }

        /** Convenience: soft coloured glow behind the glyphs. */
        public void drawGlow(String text, float x, float y, float size, int color, int glowColor) {
            drawOutlined(text, x, y, size, color, glowColor, 0.35f);
        }

        public void drawCenteredGlow(String text, float x, float y, float size, int color, int glowColor) {
            drawCenteredOutlined(text, x, y, size, color, glowColor, 0.35f);
        }

        public float getWidth(String text, float size) {
            if (text == null || text.isEmpty()) return 0;
            MsdfFont f = font();
            return f == null ? 0 : f.getWidth(text, size);
        }

        public float getHeight(float size) {
            MsdfFont f = font();
            return f == null ? size : f.getMetrics().lineHeight() * size;
        }
    }
}
