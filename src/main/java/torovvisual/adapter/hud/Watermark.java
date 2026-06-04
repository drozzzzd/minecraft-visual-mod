package torovvisual.adapter.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import torovvisual.api.system.font.FontRenderer;
import torovvisual.api.system.font.Fonts;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.math.MathUtil;

/**
 * Client watermark. Ported from the Zenith watermark draggable to Powder's Torov
 * Visual HUD base ({@link HudElement}); all Zenith references were replaced with
 * Torov Visual and the on-screen name is fixed to "TorovVisual 2.0".
 */
public class Watermark extends HudElement {

    private int fpsCount = 0;

    public Watermark() {
        super("Watermark", 10, 10, 110, 20);
    }

    @Override
    public boolean movable() {
        return true;
    }

    @Override
    public void tick() {
        fpsCount = (int) MathUtil.interpolate(fpsCount, mc.getCurrentFps());
    }

    @Override
    public void drawDraggable(DrawContext e) {
        MatrixStack matrix = e.getMatrices();
        FontRenderer font = Fonts.getSize(18, Fonts.Type.BOLD);

        String offset = "   ";
        String name = "TorovVisual 2.0" + offset;
        String fps = fpsCount + " FPS";

        float padX = 7f;
        float padY = 5f;
        float fontHeight = font.getStringHeight(name);

        // Size the panel to the text + symmetric padding so the text always sits
        // perfectly centered (equal space above and below).
        setHeight((int) (fontHeight + padY * 2));
        setWidth((int) (font.getStringWidth(name + fps) + padX * 2));

        float textY = getY() + padY;
        float nameWidth = font.getStringWidth(name);

        blur.render(ShapeProperties.create(matrix, getX(), getY(), getWidth(), getHeight())
                .round(4).softness(1).thickness(2).outlineColor(ColorUtil.getOutline()).color(ColorUtil.getRect(0.7F)).build());
        font.drawGradientString(matrix, name, getX() + padX, textY, ColorUtil.fade(0), ColorUtil.fade(100));
        font.drawString(matrix, fps, getX() + padX + nameWidth, textY, ColorUtil.getText());
        rectangle.render(ShapeProperties.create(matrix, getX() + padX + nameWidth - 4, getY() + padY, 0.5F, fontHeight)
                .color(ColorUtil.getOutline(0.75F, 0.5f)).build());
    }
}
