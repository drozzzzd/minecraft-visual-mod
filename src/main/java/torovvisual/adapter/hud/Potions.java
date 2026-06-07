package torovvisual.adapter.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import torovvisual.api.system.animation.Animation;
import torovvisual.api.system.animation.Direction;
import torovvisual.api.system.animation.implement.DecelerateAnimation;
import torovvisual.api.system.font.RichFonts;
import torovvisual.common.util.entity.PlayerIntersectionUtil;
import torovvisual.common.util.render.Render2DUtil;
import torovvisual.implement.screens.clickgui.R2D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Active potion effects, restyled to the Rich-Modern look: dark gradient card with
 * a title + count badge, then per-effect rows with the vanilla effect sprite, the
 * effect name, an optional "LVL n" tag and a timer chip. MSDF fonts via {@link RichFonts}.
 */
public class Potions extends HudElement {

    private final List<Potion> list = new ArrayList<>();
    private float animW = 80, animH = 23;
    private long lastUpdate = System.currentTimeMillis();

    public Potions() {
        super("Potions", 210, 10, 80, 23);
    }

    @Override
    public boolean movable() {
        return true;
    }

    @Override
    public boolean visible() {
        return !list.isEmpty() || PlayerIntersectionUtil.isChat(mc.currentScreen);
    }

    @Override
    public void tick() {
        if (mc.player == null) {
            list.clear();
            return;
        }
        list.removeIf(p -> p.anim.isFinished(Direction.BACKWARDS));

        List<StatusEffectInstance> active = new ArrayList<>(mc.player.getStatusEffects());
        for (Potion p : list) {
            boolean still = active.stream().anyMatch(e -> sameType(e, p.effect));
            if (!still) p.anim.setDirection(Direction.BACKWARDS);
        }
        for (StatusEffectInstance effect : active) {
            Potion existing = list.stream().filter(p -> sameType(effect, p.effect)).findFirst().orElse(null);
            if (existing == null) {
                Potion p = new Potion(effect, new DecelerateAnimation().setMs(150).setValue(1.0F));
                p.anim.setDirection(Direction.FORWARDS);
                list.add(p);
            } else {
                existing.effect = effect;
            }
        }
    }

    private static boolean sameType(StatusEffectInstance a, StatusEffectInstance b) {
        return a.getEffectType().getIdAsString().equals(b.getEffectType().getIdAsString());
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();
        R2D.begin(matrix);
        RichFonts.begin(matrix);

        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastUpdate) / 1000f, 0.1f);
        lastUpdate = now;

        float x = getX(), y = getY();

        int offset = 23;
        float targetWidth = 80;
        for (Potion p : list) {
            offset += 11;
            String name = p.effect.getEffectType().value().getName().getString();
            String lvl = p.effect.getAmplifier() > 0 ? "LVL " + (p.effect.getAmplifier() + 1) : "";
            String timer = duration(p.effect);
            float nameW = RichFonts.BOLD.getWidth(name, 6) + (lvl.isEmpty() ? 0 : 3 + RichFonts.REGULAR.getWidth(lvl, 6));
            targetWidth = Math.max(nameW + RichFonts.BOLD.getWidth(timer, 6) + 60, targetWidth);
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

        RichFonts.BOLD.draw("Potions", x + 8, y + 6.5f, 6, new Color(255, 255, 255).getRGB());
        String count = String.valueOf(Math.max(1, list.size()));
        float badgeX = x + w - RichFonts.BOLD.getWidth(count, 6) - RichFonts.BOLD.getWidth("Potions", 6) + 3;
        R2D.rect(badgeX, y + 5, 14, 12, new Color(52, 52, 52).getRGB(), 3);
        RichFonts.HUD_ICONS.draw("f", badgeX + 2, y + 6, 10, new Color(165, 165, 165).getRGB());

        int rowOffset = 23;
        for (Potion p : list) {
            float anim = p.anim.getOutput().floatValue();
            int a = (int) (255 * anim);
            StatusEffectInstance effect = p.effect;

            String name = effect.getEffectType().value().getName().getString();
            String lvl = effect.getAmplifier() > 0 ? "LVL " + (effect.getAmplifier() + 1) : "";
            String timer = duration(effect);
            float timerW = RichFonts.BOLD.getWidth(timer, 6);
            float timerBoxX = x + w - timerW - 11.5f;

            R2D.rect(timerBoxX, y + rowOffset - 2f, timerW + 4, 9, new Color(52, 52, 52, a).getRGB(), 3);
            R2D.outline(timerBoxX, y + rowOffset - 2f, timerW + 4, 9, 0.05f, new Color(132, 132, 132, a).getRGB(), 2);

            Render2DUtil.drawSprite(matrix, mc.getStatusEffectSpriteManager().getSprite(effect.getEffectType()),
                    (int) (x + 8), (int) (y + rowOffset - 2), 9, 9);

            float nameX = x + 20;
            RichFonts.BOLD.draw(name, nameX, y + rowOffset - 1.5f, 6, new Color(255, 255, 255, a).getRGB());
            if (!lvl.isEmpty()) {
                RichFonts.REGULAR.draw(lvl, nameX + RichFonts.BOLD.getWidth(name, 6) + 2, y + rowOffset - 0.5f, 5,
                        new Color(155, 155, 155, a).getRGB());
            }
            RichFonts.BOLD.draw(timer, timerBoxX + 2, y + rowOffset - 1, 6, new Color(165, 165, 165, a).getRGB());

            rowOffset += (int) (anim * 11);
        }

        R2D.scissorPop();
    }

    private static float lerp(float current, float target, float dt) {
        float f = (float) (1.0 - Math.pow(0.001, dt * 8.0));
        float v = current + (target - current) * f;
        return Math.abs(v - target) < 0.3f ? target : v;
    }

    private String duration(StatusEffectInstance e) {
        if (e.isInfinite()) return "∞∞:∞∞";
        int total = e.getDuration() / 20;
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    private static final class Potion {
        private StatusEffectInstance effect;
        private final Animation anim;

        private Potion(StatusEffectInstance effect, Animation anim) {
            this.effect = effect;
            this.anim = anim;
        }
    }
}
