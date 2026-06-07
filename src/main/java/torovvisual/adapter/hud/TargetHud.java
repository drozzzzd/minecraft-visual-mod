package torovvisual.adapter.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import torovvisual.api.system.animation.Direction;
import torovvisual.api.system.font.RichFonts;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.entity.PlayerIntersectionUtil;
import torovvisual.common.util.other.StopWatch;
import torovvisual.common.util.render.Render2DUtil;
import torovvisual.implement.screens.clickgui.R2D;

import java.awt.Color;

/**
 * Target info HUD restyled to Rich-Modern: gradient card, target face, name + HP
 * string and an animated grayscale health bar (with gold absorption). MSDF fonts.
 */
public class TargetHud extends HudElement {

    private final StopWatch stopWatch = new StopWatch();
    private LivingEntity lastTarget;
    private float displayedHealth, healthAnim, absorbAnim;
    private long lastUpdate = System.currentTimeMillis();

    public TargetHud() {
        super("TargetHud", 10, 40, 112, 40);
    }

    @Override
    public boolean movable() {
        return true;
    }

    @Override
    public boolean visible() {
        return scaleAnimation.isDirection(Direction.FORWARDS);
    }

    @Override
    public void tick() {
        if (mc.targetedEntity instanceof LivingEntity living && living.isAlive()) {
            lastTarget = living;
            startAnimation();
            stopWatch.reset();
        } else if (PlayerIntersectionUtil.isChat(mc.currentScreen)) {
            lastTarget = mc.player;
            startAnimation();
        } else if (stopWatch.finished(500)) {
            stopAnimation();
        }
    }

    @Override
    public void drawDraggable(DrawContext context) {
        if (lastTarget == null) return;
        MatrixStack matrix = context.getMatrices();
        R2D.begin(matrix);
        RichFonts.begin(matrix);

        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastUpdate) / 1000f, 0.1f);
        lastUpdate = now;

        float x = getX(), y = getY();
        setWidth(112);
        setHeight(40);

        float alpha = scaleAnimation.getOutput().floatValue();
        int a = (int) (255 * alpha);

        int[] grad = {
                new Color(52, 52, 52, a).getRGB(), new Color(22, 22, 22, a).getRGB(),
                new Color(52, 52, 52, a).getRGB(), new Color(22, 22, 22, a).getRGB()
        };
        R2D.gradientRect(x + 2, y + 2, 108, 36, grad, 6);
        R2D.outline(x + 2, y + 2, 108, 36, 0.35f, new Color(90, 90, 90, a).getRGB(), 5);

        drawFace(context);

        float hp = lastTarget.getHealth();
        float maxHp = Math.max(1, lastTarget.getMaxHealth());
        float absorp = lastTarget.getAbsorptionAmount();

        displayedHealth = lerp(displayedHealth, hp + absorp, dt, 5f);
        healthAnim = lerp(healthAnim, hp / maxHp, dt, 3f);
        absorbAnim = lerp(absorbAnim, absorp / maxHp, dt, 3f);

        float contentX = x + 34;
        float nameY = y + 13;

        RichFonts.BOLD.draw(lastTarget.getName().getString(), contentX, nameY, 5.5f, new Color(255, 255, 255, a).getRGB());
        String hpStr = healthString(displayedHealth);
        float hpW = RichFonts.BOLD.getWidth(hpStr, 5.5f);
        RichFonts.BOLD.draw(hpStr, x + 112 - 10 - hpW, nameY, 5.5f, new Color(215, 215, 215, a).getRGB());

        float barX = contentX, barY = nameY + 12, barW = 64, barH = 4;
        R2D.rect(barX, barY, barW, barH, new Color(30, 30, 30, (int) (200 * alpha)).getRGB(), 2);
        float pct = MathHelper.clamp(healthAnim, 0, 1);
        if (pct > 0.01f) R2D.rect(barX, barY, barW * pct, barH, new Color(205, 205, 205, a).getRGB(), 2);
        float absPct = MathHelper.clamp(absorbAnim, 0, 1);
        if (absPct > 0.01f) R2D.rect(barX, barY, barW * absPct, barH, new Color(255, 190, 0, (int) (210 * alpha)).getRGB(), 2);
    }

    private void drawFace(DrawContext context) {
        EntityRenderer<? super LivingEntity, ?> baseRenderer = mc.getEntityRenderDispatcher().getRenderer(lastTarget);
        if (!(baseRenderer instanceof LivingEntityRenderer<?, ?, ?>)) return;

        @SuppressWarnings("unchecked")
        LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?> renderer =
                (LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>) baseRenderer;
        LivingEntityRenderState state = renderer.getAndUpdateRenderState(lastTarget, tickCounter.getTickDelta(false));
        Identifier textureLocation = renderer.getTexture(state);

        Render2DUtil.drawTexture(context, textureLocation, getX() + 6, getY() + 8, 24, 3, 8, 8, 64,
                ColorUtil.getRect(1), ColorUtil.multRed(-1, 1 + lastTarget.hurtTime / 4F));
    }

    private static float lerp(float current, float target, float dt, float speed) {
        float f = (float) (1.0 - Math.pow(0.001, dt * speed));
        return current + (target - current) * f;
    }

    private String healthString(float health) {
        if (health >= 100) return String.valueOf((int) health);
        if (health >= 10) return String.format("%.1f", health);
        return String.format("%.2f", health);
    }
}
