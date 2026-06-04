package powder.client.addon.addons.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import org.joml.Vector4i;

import powder.api.event.EventSubscribe;
import powder.api.event.events.EventRender3D;
import powder.api.math.MathUtil;
import powder.api.render.level.Render3DUtil;
import powder.api.render.providers.ThemeProvider;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.CheckBoxWidget;
import powder.client.gui.widget.widgets.SliderWidget;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Spawns an expanding, fading glowing ring on the ground whenever a player jumps.
 * The ring is the {@code circle.png} texture drawn flat on the ground (additive),
 * growing via an ease-out curve and fading over its lifetime. Rendering goes
 * through the shared {@link Render3DUtil#drawGlowTexture} helper (the same path
 * used by the ESP glow) so it reliably shows regardless of prior GL state.
 */
public final class JumpCircle extends Addon {

    public static JumpCircle INSTANCE;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    @IWidget public final SliderWidget radius     = new SliderWidget(1, 30);   // /10 -> 0.1..3.0 blocks
    @IWidget public final SliderWidget growth     = new SliderWidget(1, 40);   // /10 extra radius from animation
    @IWidget public final SliderWidget lifeTime   = new SliderWidget(5, 60);   // /10 seconds
    @IWidget public final CheckBoxWidget selfEffect = new CheckBoxWidget("Self");
    @IWidget public final CheckBoxWidget others    = new CheckBoxWidget("Others");

    private static final Identifier CIRCLE = Identifier.of("mre", "textures/circle.png");

    private final Map<UUID, Boolean> wasOnGround = new HashMap<>();
    private final CopyOnWriteArrayList<Circle> circles = new CopyOnWriteArrayList<>();

    public JumpCircle() {
        super("JumpCircle", Type.VISUAL);
        INSTANCE = this;
        this.radius.currentValue = 5f;
        this.growth.currentValue = 10f;
        this.lifeTime.currentValue = 16f;
        this.selfEffect.isActive = true;
        this.others.isActive = true;
        super.addWidget(this.radius, this.growth, this.lifeTime, this.selfEffect, this.others);
    }

    @Override
    public void disable() {
        super.disable();
        circles.clear();
        wasOnGround.clear();
    }

    @EventSubscribe
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        // Spawn one ring per real jump: was on ground, now airborne AND moving up
        // (so stepping/falling off a ledge doesn't spawn). Capped to avoid overlap.
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!selfEffect.isActive && player == mc.player) continue;
            if (!others.isActive && player != mc.player) continue;

            UUID id = player.getUuid();
            boolean onGround = player.isOnGround();
            boolean prev = wasOnGround.getOrDefault(id, true);
            if (prev && !onGround && player.getVelocity().y > 0.07 && circles.size() < 12) {
                circles.add(new Circle(MathUtil.interpolate(player).add(0, 0.05, 0)));
            }
            wasOnGround.put(id, onGround);
        }

        renderCircles(event.getMatrix());
    }

    private void renderCircles(MatrixStack matrices) {
        if (circles.isEmpty()) return;

        long life = (long) (lifeTime.currentValue / 10f * 1000f);
        Vec3d cam = mc.getEntityRenderDispatcher().camera.getPos();

        Iterator<Circle> it = circles.iterator();
        while (it.hasNext()) {
            Circle c = it.next();
            long age = System.currentTimeMillis() - c.time;
            if (age > life) { circles.remove(c); continue; }

            float t = Math.min(1f, age / (float) life);
            float fade = 1f - t;
            float eased = circOut(Math.min(1f, age / (float) Math.max(1L, life / 2)));
            float rad = eased * (growth.currentValue / 10f) + (radius.currentValue / 10f);

            int alpha = (int) (255 * fade);
            int color = ThemeProvider.getClientColor((int) (t * 200)).withAlpha(alpha).getRGB();

            float cx = (float) (c.pos.x - cam.x);
            float cy = (float) (c.pos.y - cam.y);
            float cz = (float) (c.pos.z - cam.z);

            matrices.push();
            matrices.translate(cx, cy, cz);
            // Lay the quad flat on the ground (local XY plane -> world XZ plane).
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f));
            Render3DUtil.drawGlowTexture(matrices.peek(), CIRCLE, -rad, -rad, rad * 2f, rad * 2f,
                    new Vector4i(color, color, color, color), false);
            matrices.pop();
        }
    }

    private static float circOut(float t) {
        return (float) Math.sqrt(1.0 - Math.pow(t - 1.0, 2.0));
    }

    private static final class Circle {
        private final Vec3d pos;
        private final long time;

        private Circle(Vec3d pos) {
            this.pos = pos;
            this.time = System.currentTimeMillis();
        }
    }
}
