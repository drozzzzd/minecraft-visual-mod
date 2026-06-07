package torovvisual.adapter.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import torovvisual.api.feature.module.Module;
import torovvisual.api.system.font.RichFonts;
import torovvisual.common.util.entity.PlayerIntersectionUtil;
import torovvisual.common.util.other.StringUtil;
import torovvisual.core.Main;
import torovvisual.implement.screens.clickgui.R2D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * "Binds" keybind list, restyled to the Rich-Modern look: dark gradient card with
 * outline, a title + active-count badge, and per-module rows with a bracketed
 * keybind chip. Drawn with the source MSDF fonts ({@link RichFonts}).
 */
public class HotKeys extends HudElement {

    private List<Module> keysList = new ArrayList<>();
    private float animW = 80, animH = 23;
    private long lastUpdate = System.currentTimeMillis();

    public HotKeys() {
        super("HotKeys", 300, 10, 80, 23);
    }

    @Override
    public boolean movable() {
        return true;
    }

    @Override
    public boolean visible() {
        return !keysList.isEmpty() || PlayerIntersectionUtil.isChat(mc.currentScreen);
    }

    @Override
    public void tick() {
        keysList = Main.getInstance().getModuleProvider().getModules().stream()
                .filter(module -> module.isState() && module.getKey() != -1)
                .toList();
    }

    private static float lerp(float current, float target, float dt) {
        float f = (float) (1.0 - Math.pow(0.001, dt * 8.0));
        float v = current + (target - current) * f;
        return Math.abs(v - target) < 0.3f ? target : v;
    }

    @Override
    public void drawDraggable(DrawContext e) {
        MatrixStack matrix = e.getMatrices();
        R2D.begin(matrix);
        RichFonts.begin(matrix);

        float x = getX(), y = getY();

        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastUpdate) / 1000f, 0.1f);
        lastUpdate = now;

        boolean showExample = keysList.isEmpty() && PlayerIntersectionUtil.isChat(mc.currentScreen);

        int offset = 23;
        float targetWidth = 80;
        List<String[]> rows = new ArrayList<>();

        if (showExample) {
            rows.add(new String[]{"Example Module", "[A]"});
        } else {
            for (Module module : keysList) {
                rows.add(new String[]{module.getVisibleName(), "[" + StringUtil.getBindName(module.getKey()) + "]"});
            }
        }
        for (String[] row : rows) {
            offset += 11;
            float nameW = RichFonts.BOLD.getWidth(row[0], 6);
            float bindW = RichFonts.BOLD.getWidth(row[1], 6);
            targetWidth = Math.max(nameW + bindW + 50, targetWidth);
        }

        float targetHeight = offset + 2;
        animW = lerp(animW, targetWidth, dt);
        animH = lerp(animH, targetHeight, dt);
        setWidth((int) Math.ceil(animW));
        setHeight((int) Math.ceil(animH));

        int w = getWidth();
        int[] grad = {
                new Color(52, 52, 52).getRGB(), new Color(32, 32, 32).getRGB(),
                new Color(52, 52, 52).getRGB(), new Color(32, 32, 32).getRGB()
        };
        R2D.gradientRect(x, y, w, animH, grad, 5);
        R2D.outline(x, y, w, animH, 0.35f, new Color(90, 90, 90).getRGB(), 5);

        R2D.scissorPush(x, y, w, animH);

        // title + active count badge
        RichFonts.BOLD.draw("Binds", x + 8, y + 6.5f, 6, new Color(255, 255, 255).getRGB());
        String count = String.valueOf(keysList.size());
        float countW = RichFonts.BOLD.getWidth(count, 6);
        float badgeX = x + w - countW - RichFonts.BOLD.getWidth("Active:", 6) + 2;
        R2D.rect(badgeX, y + 5, 14, 12, new Color(52, 52, 52).getRGB(), 3);
        RichFonts.HUD_ICONS.draw("g", badgeX + 2, y + 6, 10, new Color(165, 165, 165).getRGB());

        int rowOffset = 23;
        for (String[] row : rows) {
            String name = row[0], bind = row[1];
            float bindW = RichFonts.BOLD.getWidth(bind, 6);
            float bindBoxX = x + w - bindW - 11.5f;

            R2D.rect(bindBoxX, y + rowOffset - 2f, bindW + 4, 9, new Color(52, 52, 52).getRGB(), 3);
            R2D.outline(bindBoxX, y + rowOffset - 2f, bindW + 4, 9, 0.05f, new Color(132, 132, 132).getRGB(), 2);
            R2D.rect(x + 8, y + rowOffset - 1, 1f, 7, new Color(155, 155, 155, 128).getRGB(), 1);
            RichFonts.BOLD.draw(name, x + 13, y + rowOffset - 1.5f, 6, new Color(255, 255, 255).getRGB());
            RichFonts.BOLD.draw(bind, bindBoxX + 2, y + rowOffset - 1, 6, new Color(165, 165, 165).getRGB());
            rowOffset += 11;
        }

        R2D.scissorPop();
    }
}
