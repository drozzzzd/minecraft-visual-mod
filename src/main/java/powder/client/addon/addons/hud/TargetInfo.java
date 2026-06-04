package powder.client.addon.addons.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.glfw.GLFW;

import powder.Powder;
import powder.api.event.EventSubscribe;
import powder.api.event.events.EventRender2D;
import powder.api.math.MathSystem;
import powder.api.render.drawing.DrawSystem;
import powder.api.render.drawing.TextSystem;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.IStyle;
import powder.client.gui.screens.draggableGui.comp.Draggable;

import java.awt.*;

public class TargetInfo extends Addon implements IStyle {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private LivingEntity target;
    private float anim;
    private float healthAnimation;
    private float absorptionAnimation;
    private float scrollOffset;

    public TargetInfo() {
        super("TargetInfo", GLFW.GLFW_KEY_N, Type.HUD);
        Draggable.draggables.add(new Draggable(this.getName(), 150f, 90f, 110f, 38f));
    }

    @EventSubscribe
    public void eventRender2D(EventRender2D event) {
        LivingEntity current = getCrosshairTarget();
        if (current != null) target = current;

        anim = MathSystem.lerp(anim, current != null ? 1f : 0f, 0.18f);
        if (anim <= 0.02f || target == null) {
            scrollOffset = 0f;
            return;
        }

        int alpha = (int) (anim * 255f);

        Draggable draggable = Draggable.byName(this.getName());
        float x = draggable != null ? draggable.x : 150f;
        float y = draggable != null ? draggable.y : 90f;

        float paddingX = 6f;
        float paddingY = 5f;
        float headSize = 26f;
        float gapHeadText = 8f;
        float barHeight = 6f;
        float textZoneWidth = 70f;

        float nicknameHeight = 12f;
        float hpTextHeight = 11f;

        float totalWidth = paddingX + headSize + gapHeadText + textZoneWidth + paddingX;
        float totalHeight = paddingY * 2f + headSize;

        if (draggable != null) {
            draggable.width = totalWidth;
            draggable.height = totalHeight;
        }

        DrawSystem.drawBlur(event.getGraphics(), x, y, totalWidth, totalHeight, 5, 5, 5, 5,
                new Color(23, 23, 23, alpha).getRGB());

        float headX = x + paddingX;
        float headY = y + (totalHeight - headSize) / 2f;

        if (target instanceof PlayerEntity player) {
            Identifier skin = mc.getSkinProvider().getSkinTextures(player.getGameProfile()).texture();

            float hurt = target.hurtTime / 10f;
            int tint = hurt > 0f
                    ? new Color(255, (int) (255f * (1f - hurt)), (int) (255f * (1f - hurt)), alpha).getRGB()
                    : new Color(255, 255, 255, alpha).getRGB();

            DrawSystem.drawTexture(event.getGraphics(), skin, headX, headY, headSize, headSize,
                    0.125f, 0.128f, 0.125f, 0.120f, 4, 4, 4, 4, tint);
        }

        float healthPct = MathHelper.clamp(target.getHealth() / target.getMaxHealth(), 0f, 1f);
        healthAnimation = MathSystem.lerp(healthAnimation, healthPct, 0.12f);

        float absorptionPct = MathHelper.clamp(target.getAbsorptionAmount() / 20f, 0f, 1f);
        absorptionAnimation = MathSystem.lerp(absorptionAnimation, absorptionPct, 0.12f);

        float contentX = headX + headSize + gapHeadText;
        float contentY = y + paddingY + 2f;

        String name = target.getName().getString();
        float nameWidth = Powder.INTER_FONT.get().getWidth(name, 11);

        if (nameWidth > textZoneWidth) {
            float delta = mc.getRenderTickCounter().getTickDelta(true);
            scrollOffset += (30f * delta) / 20f;
            float totalScroll = nameWidth + textZoneWidth;
            if (scrollOffset > totalScroll) scrollOffset -= totalScroll;

            float drawX = contentX - scrollOffset;
            DrawSystem.drawScissor(contentX, contentY, textZoneWidth, nicknameHeight, () -> {
                TextSystem.drawText(event.getGraphics(), name, drawX, contentY, 11, new Color(255, 255, 255, alpha).getRGB());
                TextSystem.drawText(event.getGraphics(), name, drawX + totalScroll, contentY, 11, new Color(255, 255, 255, alpha).getRGB());
            });
        } else {
            scrollOffset = 0f;
            TextSystem.drawText(event.getGraphics(), name, contentX, contentY, 11, new Color(255, 255, 255, alpha).getRGB());
        }

        float hp = target.getHealth();
        String hpText = hp == (int) hp ? "HP: " + (int) hp + ".0" : "HP: " + String.format("%.1f", hp);
        float hpTextY = contentY + nicknameHeight + 2f;
        TextSystem.drawText(event.getGraphics(), hpText, contentX, hpTextY, 10,
                new Color(190, 190, 200, (int) (alpha * 0.85f)).getRGB());

        float barY = hpTextY + hpTextHeight + 3f;
        DrawSystem.drawRectangle(event.getGraphics(), contentX, barY, textZoneWidth, barHeight, 1.5f, 1.5f, 1.5f, 1.5f,
                new Color(40, 40, 45, alpha).getRGB());

        float healthWidth = textZoneWidth * healthAnimation;
        if (healthWidth > 1f) {
            int left, right;
            if (healthPct > 0.6f) {
                left = new Color(30, 140, 60, alpha).getRGB();
                right = new Color(80, 220, 100, alpha).getRGB();
            } else if (healthPct > 0.3f) {
                left = new Color(200, 120, 30, alpha).getRGB();
                right = new Color(255, 180, 60, alpha).getRGB();
            } else {
                left = new Color(180, 30, 30, alpha).getRGB();
                right = new Color(255, 70, 70, alpha).getRGB();
            }
            DrawSystem.drawGradient(event.getGraphics(), contentX, barY, healthWidth, barHeight, 1.5f, 1.5f, 1.5f, 1.5f, left, right);
        }

        if (absorptionAnimation > 0.01f) {
            float absWidth = textZoneWidth * absorptionAnimation;
            DrawSystem.drawGradient(event.getGraphics(), contentX, barY, absWidth, barHeight, 1.5f, 1.5f, 1.5f, 1.5f,
                    new Color(180, 140, 20, alpha).getRGB(), new Color(255, 215, 50, alpha).getRGB());
        }
    }

    private LivingEntity getCrosshairTarget() {
        if (mc.player == null || mc.world == null) return null;
        if (mc.targetedEntity instanceof LivingEntity living && living.isAlive() && living != mc.player)
            return living;
        return null;
    }

}
