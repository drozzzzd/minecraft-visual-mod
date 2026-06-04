package torovvisual.adapter.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Formatting;
import torovvisual.api.system.animation.Animation;
import torovvisual.api.system.animation.Direction;
import torovvisual.api.system.animation.implement.DecelerateAnimation;
import torovvisual.api.system.font.FontRenderer;
import torovvisual.api.system.font.Fonts;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.entity.PlayerIntersectionUtil;
import torovvisual.common.util.math.MathUtil;
import torovvisual.common.util.render.Render2DUtil;

import java.util.ArrayList;
import java.util.List;

public class Potions extends HudElement {
    private final List<Potion> list = new ArrayList<>();

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
                list.add(new Potion(effect, new DecelerateAnimation().setMs(150).setValue(1.0F)));
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
        FontRenderer font = Fonts.getSize(15, Fonts.Type.DEFAULT);
        FontRenderer fontPotion = Fonts.getSize(13, Fonts.Type.DEFAULT);

        blur.render(ShapeProperties.create(matrix, getX(), getY(), getWidth(), 17.5F)
                .round(4, 0, 4, 0).softness(1).thickness(2).outlineColor(ColorUtil.getOutline()).color(ColorUtil.getRectDarker(0.9F)).build());

        blur.render(ShapeProperties.create(matrix, getX(), getY() + 17F, getWidth(), getHeight() - 17)
                .round(0, 4, 0, 4).softness(1).thickness(2).outlineColor(ColorUtil.getOutline()).color(ColorUtil.getRect(0.7F)).build());

        float centerX = getX() + getWidth() / 2.0F;
        int offset = 23, maxWidth = 80;

        font.drawString(matrix, getName(), (int) (centerX - font.getStringWidth(getName()) / 2.0F), getY() + 7, ColorUtil.getText());
        for (Potion potion : list) {
            StatusEffectInstance effect = potion.effect;
            float animation = potion.anim.getOutput().floatValue();
            float centerY = getY() + offset;
            int amplifier = effect.getAmplifier();

            String name = effect.getEffectType().value().getName().getString();
            String duration = getDuration(effect);
            String lvl = amplifier > 0 ? Formatting.RED + " " + (amplifier + 1) + Formatting.RESET : "";

            MathUtil.scale(matrix, centerX, centerY, 1, animation, () -> {
                float animRed = effect.getDuration() != -1 && effect.getDuration() <= 120 ? MathUtil.blinking(1000, 8) : 1;
                Render2DUtil.drawSprite(matrix, mc.getStatusEffectSpriteManager().getSprite(effect.getEffectType()), getX() + 5, (int) centerY - 2, 8, 8);
                rectangle.render(ShapeProperties.create(matrix, getX() + 14, centerY - 1, 0.5F, 7).color(ColorUtil.getOutline(0.75F, 0.5F)).build());
                fontPotion.drawString(matrix, name + lvl, getX() + 18, centerY + 1, ColorUtil.getText());
                fontPotion.drawString(matrix, duration, getX() + getWidth() - 5 - fontPotion.getStringWidth(duration), centerY + 1, ColorUtil.multRed(ColorUtil.getText(), animRed));
            });

            int width = (int) fontPotion.getStringWidth(name + lvl + duration) + 30;
            maxWidth = Math.max(width, maxWidth);
            offset += (int) (11 * animation);
        }

        setWidth(maxWidth);
        setHeight(offset);
    }

    private String getDuration(StatusEffectInstance pe) {
        int var1 = pe.getDuration();
        int mins = var1 / 1200;
        return pe.isInfinite() || mins > 60 ? "**:**" : mins + ":" + String.format("%02d", (var1 % 1200) / 20);
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
