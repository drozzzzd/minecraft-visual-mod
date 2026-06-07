package torovvisual.adapter.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import ru.kotopushka.compiler.sdk.classes.Profile;
import torovvisual.api.system.font.FontRenderer;
import torovvisual.api.system.font.Fonts;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.math.MathUtil;

/**
 * Client watermark. Logo box on the left (glass "T" logo) followed by the
 * username, FPS and ping separated by thin dividers, wrapped in an outlined
 * panel. Ported to Powder's Torov Visual HUD base ({@link HudElement}) and
 * rendered with the Torov Visual draw system (fonts / shapes / image).
 */
public class Watermark extends HudElement {

    private static final String LOGO = "textures/watermark_logo.png";

    private int fpsCount = 0;

    public Watermark() {
        super("Watermark", 10, 10, 120, 20);
    }

    @Override
    public boolean movable() {
        return true;
    }

    @Override
    public void tick() {
        fpsCount = (int) MathUtil.interpolate(fpsCount, mc.getCurrentFps());
    }

    private int ping() {
        if (mc.player != null && mc.getNetworkHandler() != null
                && mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()) != null) {
            return mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()).getLatency();
        }
        return 0;
    }

    @Override
    public void drawDraggable(DrawContext e) {
        MatrixStack matrix = e.getMatrices();
        FontRenderer font = Fonts.getSize(14, Fonts.Type.BOLD);

        String name = Profile.getUsername();
        String fps = fpsCount + " FPS";
        String ping = ping() + " MS";

        float height = 20f;
        float logoSize = 15f;
        float logoPad = 3f;
        float logoBoxWidth = logoSize + logoPad * 2f;

        float padX = 6f;
        float gap = 5f;
        float separator = 1f;

        float nameWidth = font.getStringWidth(name);
        float fpsWidth = font.getStringWidth(fps);
        float pingWidth = font.getStringWidth(ping);

        float contentWidth = nameWidth + gap + separator + gap + fpsWidth + gap + separator + gap + pingWidth;
        float mainBoxWidth = contentWidth + padX * 2f;
        float totalWidth = logoBoxWidth + mainBoxWidth;

        setWidth((int) totalWidth);
        setHeight((int) height);

        float x = getX();
        float y = getY();
        float textY = y + height / 2f - 4f;

        MathUtil.scale(matrix, x + totalWidth / 2f, y + height / 2f, scaleAnimation.getOutput().floatValue(), () -> {
            // Panel background + outline (consistent with the rest of the HUD).
            blur.render(ShapeProperties.create(matrix, x, y, totalWidth, height)
                    .round(4).softness(1).thickness(2)
                    .outlineColor(ColorUtil.getOutline()).color(ColorUtil.getRect(0.7F)).build());

            // Logo box: glass "T" logo, then a divider to the content area.
            image.setTexture(LOGO).render(ShapeProperties.create(matrix,
                    x + logoPad, y + (height - logoSize) / 2f, logoSize, logoSize).color(0xFFFFFFFF).build());
            rectangle.render(ShapeProperties.create(matrix, x + logoBoxWidth, y + 4f, separator, height - 8f)
                    .color(ColorUtil.getOutline(0.75F, 0.5f)).build());

            float cx = x + logoBoxWidth + padX;

            font.drawGradientString(matrix, name, cx, textY, ColorUtil.fade(0), ColorUtil.fade(100));
            cx += nameWidth + gap;
            cx = divider(matrix, cx, y, height, separator);

            font.drawString(matrix, fps, cx, textY, ColorUtil.getText());
            cx += fpsWidth + gap;
            cx = divider(matrix, cx, y, height, separator);

            font.drawString(matrix, ping, cx, textY, ColorUtil.getText());
        });
    }

    private float divider(MatrixStack matrix, float cx, float y, float height, float separator) {
        rectangle.render(ShapeProperties.create(matrix, cx, y + 5f, separator, height - 10f)
                .color(ColorUtil.getOutline(0.75F, 0.5f)).build());
        return cx + separator + 5f;
    }
}
