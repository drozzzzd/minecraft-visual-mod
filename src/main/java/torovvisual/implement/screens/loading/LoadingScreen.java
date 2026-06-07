package torovvisual.implement.screens.loading;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import torovvisual.api.system.font.RichFonts;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.common.QuickImports;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.math.MathUtil;
import torovvisual.implement.screens.menu.mainmenu.MainMenu;

/**
 * Branded Torov Visual loading splash shown once at startup before the main menu:
 * logo, glowing title, an animated progress bar and a percent counter, then it
 * hands off to {@link MainMenu}.
 */
public class LoadingScreen extends Screen implements QuickImports {

    private float progress = 0f;
    private float displayed = 0f;

    public LoadingScreen() {
        super(Text.literal("Loading"));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        RichFonts.begin(matrix);

        // dark gradient backdrop
        rectangle.render(ShapeProperties.create(matrix, 0, 0, this.width, this.height)
                .color(0xFF0C0C0E, 0xFF050506, 0xFF0C0C0E, 0xFF050506).build());

        progress = Math.min(1f, progress + 0.010f);
        displayed = MathUtil.interpolateSmooth(6, displayed, progress);

        float cx = this.width / 2f;
        float logoW = 170, logoH = 54;
        image.setTexture("textures/torov_logo.png").render(ShapeProperties.create(matrix, cx - logoW / 2f, this.height / 2f - 70, logoW, logoH)
                .color(0xFFFFFFFF).build());

        RichFonts.BOLD.drawCenteredGlow("Torov Visual", cx, this.height / 2f - 6, 22, 0xFFFFFFFF,
                ColorUtil.multAlpha(ColorUtil.getClientColor(), 0.45F));

        // progress bar
        float barW = 200, barH = 4;
        float barX = cx - barW / 2f, barY = this.height / 2f + 26;
        rectangle.render(ShapeProperties.create(matrix, barX, barY, barW, barH).round(2).color(ColorUtil.getColor(28, 28, 32)).build());
        rectangle.render(ShapeProperties.create(matrix, barX, barY, barW * displayed, barH).round(2)
                .softness(2).color(ColorUtil.multAlpha(ColorUtil.getClientColor(), 0.35F)).build());
        rectangle.render(ShapeProperties.create(matrix, barX, barY, barW * displayed, barH).round(2)
                .color(ColorUtil.getClientColor()).build());

        RichFonts.BOLD.drawCentered("Loading... " + (int) (displayed * 100) + "%", cx, barY + 10, 7,
                ColorUtil.getColor(190, 190, 200, 220));

        RichFonts.BOLD.drawCentered("Torov Visual © All Rights Reserved", cx, this.height - 12, 6,
                ColorUtil.getColor(120, 120, 128, 160));

        if (displayed >= 0.999f) {
            mc.setScreen(new MainMenu());
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
