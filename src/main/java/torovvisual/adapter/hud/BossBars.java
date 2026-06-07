package torovvisual.adapter.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.BossBar;
import torovvisual.api.system.font.RichFonts;
import torovvisual.implement.screens.clickgui.R2D;

import java.awt.Color;

public class BossBars extends HudElement {
    public BossBars() {
        super("Boss Bars", 0, 0, 0, 0);
    }

    @Override
    public boolean visible() {
        return !mc.inGameHud.getBossBarHud().bossBars.isEmpty();
    }

    @Override
    public void drawDraggable(DrawContext context) {
        setX(mc.getWindow().getScaledWidth() / 2);

        MatrixStack matrix = context.getMatrices();
        R2D.begin(matrix);
        RichFonts.begin(matrix);

        float y = 10;
        float width = 156;
        float height = 4F;

        int[] grad = {
                new Color(52, 52, 52).getRGB(), new Color(32, 32, 32).getRGB(),
                new Color(52, 52, 52).getRGB(), new Color(32, 32, 32).getRGB()
        };

        for (ClientBossBar bossInfo : mc.inGameHud.getBossBarHud().bossBars.values()) {
            int color = getColor(bossInfo.getColor());
            float barX = getX() - width / 2;

            R2D.gradientRect(barX, y + 11, width, height, grad, height / 2);
            R2D.rect(barX, y + 11, width * bossInfo.getPercent(), height, color, height / 2);
            R2D.outline(barX, y + 11, width, height, 0.35f, new Color(90, 90, 90).getRGB(), height / 2);

            String name = bossInfo.getName().getString();
            RichFonts.BOLD.drawCentered(name, getX(), y, 6, new Color(255, 255, 255).getRGB());
            y += 22;
        }
    }

    public int getColor(BossBar.Color color) {
        return switch (color) {
            case PINK -> new Color(0xFF5AB4).getRGB();
            case PURPLE -> new Color(0x813CFF).getRGB();
            case RED -> new Color(0xFF3737).getRGB();
            case BLUE -> new Color(0x00A0FF).getRGB();
            case GREEN -> new Color(0x55FF55).getRGB();
            default -> new Color(color.getTextFormat().getColorValue()).getRGB();
        };
    }
}
