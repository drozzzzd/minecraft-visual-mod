package torovvisual.implement.screens.clickgui;

import net.minecraft.client.util.math.MatrixStack;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.api.system.shape.implement.Rectangle;
import torovvisual.common.util.render.ScissorManager;
import torovvisual.core.Main;

/**
 * Thin re-implementation of Rich-Modern's {@code Render2D} primitive API on top of
 * Torov Visual's rounded-rect shader, so the ClickGUI renderers port over almost
 * verbatim. The matrix is set once per frame via {@link #begin(MatrixStack)}.
 */
public final class R2D {

    private static final Rectangle RECT = new Rectangle();
    private static MatrixStack matrix;

    private R2D() {}

    public static void begin(MatrixStack stack) {
        matrix = stack;
    }

    public static void rect(float x, float y, float w, float h, int color) {
        rect(x, y, w, h, color, 0);
    }

    public static void rect(float x, float y, float w, float h, int color, float radius) {
        RECT.render(ShapeProperties.create(matrix, x, y, w, h).round(radius).color(color).build());
    }

    /** Vertical-ish gradient; maps up to four colours to the rounded-rect corners. */
    public static void gradientRect(float x, float y, float w, float h, int[] colors, float radius) {
        int c0 = colors[0];
        int c1 = colors.length > 1 ? colors[1] : c0;
        int c2 = colors.length > 2 ? colors[2] : c1;
        int c3 = colors.length > 3 ? colors[3] : c2;
        RECT.render(ShapeProperties.create(matrix, x, y, w, h).round(radius).color(c0, c1, c2, c3).build());
    }

    public static void outline(float x, float y, float w, float h, float thickness, int color, float radius) {
        RECT.render(ShapeProperties.create(matrix, x, y, w, h).round(radius)
                .thickness(Math.max(1f, thickness * 2f)).softness(0.5f).outlineColor(color).color(0x00000000).build());
    }

    public static void outline(float x, float y, float w, float h, float thickness, int color) {
        outline(x, y, w, h, thickness, color, 0);
    }

    /** Rich's subtle frosted box — approximated with a soft translucent rounded rect. */
    public static void blur(float x, float y, float w, float h, float blurRadius, float cornerRadius, int tint) {
        RECT.render(ShapeProperties.create(matrix, x, y, w, h).round(cornerRadius).softness(blurRadius).color(tint).build());
    }

    public static void scissorPush(float x, float y, float w, float h) {
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.peek().getPositionMatrix(), x, y, w, h);
    }

    public static void scissorPop() {
        Main.getInstance().getScissorManager().pop();
    }
}
