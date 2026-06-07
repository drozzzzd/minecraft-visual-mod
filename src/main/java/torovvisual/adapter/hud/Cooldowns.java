package torovvisual.adapter.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import torovvisual.api.system.font.RichFonts;
import torovvisual.common.util.entity.PlayerIntersectionUtil;
import torovvisual.common.util.render.Render2DUtil;
import torovvisual.implement.screens.clickgui.R2D;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Item cooldown HUD restyled to Rich-Modern: gradient card with a title + icon
 * badge, then per-item rows (item icon, name, estimated-time chip). MSDF fonts.
 */
public class Cooldowns extends HudElement {

    private final Map<Item, CoolInfo> infos = new LinkedHashMap<>();
    private float animW = 80, animH = 23;
    private long lastUpdate = System.currentTimeMillis();

    public Cooldowns() {
        super("Cooldowns", 10, 60, 90, 30);
    }

    @Override
    public boolean movable() {
        return true;
    }

    @Override
    public boolean visible() {
        return mc.player != null && (!infos.isEmpty() || PlayerIntersectionUtil.isChat(mc.currentScreen));
    }

    @Override
    public void drawDraggable(DrawContext context) {
        if (mc.player == null) return;
        MatrixStack matrix = context.getMatrices();
        R2D.begin(matrix);
        RichFonts.begin(matrix);

        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastUpdate) / 1000f, 0.1f);
        lastUpdate = now;

        // collect cooling items
        var cdm = mc.player.getItemCooldownManager();
        Set<Item> active = new HashSet<>();
        Set<Item> seen = new HashSet<>();
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !seen.add(stack.getItem())) continue;
            if (cdm.isCoolingDown(stack)) {
                active.add(stack.getItem());
                infos.computeIfAbsent(stack.getItem(), it -> new CoolInfo(cdm.getCooldownProgress(stack, 0f)));
            }
        }
        infos.keySet().removeIf(it -> !active.contains(it));

        float x = getX(), y = getY();
        float timerW = RichFonts.BOLD.getWidth("00:00", 6);

        int offset = 23;
        float targetWidth = 80;
        for (Item item : infos.keySet()) {
            offset += 11;
            float nameW = RichFonts.BOLD.getWidth(item.getDefaultStack().getName().getString(), 6);
            targetWidth = Math.max(nameW + timerW + 55, targetWidth);
        }
        animW = lerp(animW, targetWidth, dt);
        animH = lerp(animH, offset + 2, dt);
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

        R2D.rect(x + w - 22.5f, y + 5, 14, 12, new Color(52, 52, 52).getRGB(), 3);
        RichFonts.ICONS.draw("D", x + w - 20f, y + 6.5f, 9, new Color(165, 165, 165).getRGB());
        RichFonts.BOLD.draw("CoolDowns", x + 8, y + 6.5f, 6, new Color(255, 255, 255).getRGB());

        float boxW = timerW + 4;
        float boxX = x + w - boxW - 9.5f;
        int rowOffset = 23;
        for (Map.Entry<Item, CoolInfo> entry : infos.entrySet()) {
            Item item = entry.getKey();
            ItemStack stack = item.getDefaultStack();
            float progress = cdm.getCooldownProgress(stack, 0f);
            entry.getValue().update(progress);
            String duration = entry.getValue().text(progress);
            String name = stack.getName().getString();

            R2D.rect(boxX + 1, y + rowOffset - 1f, boxW, 9, new Color(52, 52, 52).getRGB(), 3);
            R2D.outline(boxX + 1, y + rowOffset - 1f, boxW, 9, 0.05f, new Color(132, 132, 132).getRGB(), 2);

            Render2DUtil.defaultDrawStack(context, stack, x + 8, y + rowOffset - 1f, false, false, 0.5F);

            RichFonts.BOLD.draw(name, x + 20, y + rowOffset - 0.5f, 6, new Color(255, 255, 255).getRGB());
            float dW = RichFonts.BOLD.getWidth(duration, 6);
            RichFonts.BOLD.draw(duration, boxX + (boxW - dW) / 2 + 1, y + rowOffset, 6, new Color(165, 165, 165).getRGB());

            rowOffset += 11;
        }

        R2D.scissorPop();
    }

    private static float lerp(float current, float target, float dt) {
        float f = (float) (1.0 - Math.pow(0.001, dt * 8.0));
        float v = current + (target - current) * f;
        return Math.abs(v - target) < 0.3f ? target : v;
    }

    /** Estimates the remaining cooldown seconds from how fast progress falls. */
    private static final class CoolInfo {
        final long start = System.currentTimeMillis();
        final float startProgress;
        long totalMs;

        CoolInfo(float startProgress) {
            this.startProgress = startProgress;
        }

        void update(float progress) {
            if (totalMs > 0) return;
            long elapsed = System.currentTimeMillis() - start;
            float consumed = startProgress - progress;
            if (elapsed > 200 && consumed > 0.01f) {
                totalMs = (long) (elapsed / consumed);
            }
        }

        String text(float progress) {
            if (progress <= 0) return "0:00";
            if (totalMs <= 0) return "...";
            int secs = (int) Math.ceil(progress * totalMs / 1000.0);
            return String.format("%d:%02d", secs / 60, secs % 60);
        }
    }
}
