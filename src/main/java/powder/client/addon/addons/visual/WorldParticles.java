package powder.client.addon.addons.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import org.joml.Vector4i;

import powder.api.event.EventSubscribe;
import powder.api.event.events.EventRender3D;
import powder.api.render.level.Render3DUtil;
import powder.api.render.providers.ThemeProvider;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.CheckBoxWidget;
import powder.client.gui.widget.widgets.SliderWidget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Ambient world particles: glowing billboards (the p1/p2/p3 textures) that float
 * around the player, drift upward and fade out. Written fresh on Powder's 1.21.4
 * render API (the provided Pulse sources were obfuscated decompiled code and could
 * not be ported). Drawn additively through {@link Render3DUtil#drawGlowTexture}.
 */
public final class WorldParticles extends Addon {

    public static WorldParticles INSTANCE;

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Random random = new Random();

    @IWidget public final SliderWidget count    = new SliderWidget(0, 200);  // target particle count
    @IWidget public final SliderWidget size      = new SliderWidget(1, 30);   // /10 -> 0.1..3.0
    @IWidget public final SliderWidget speed     = new SliderWidget(0, 30);   // /100 upward drift
    @IWidget public final SliderWidget spawnArea  = new SliderWidget(4, 48);   // radius around player
    @IWidget public final SliderWidget mode       = new SliderWidget(0, 1);    // 2 modes: 0 = star1, 1 = star2
    @IWidget public final CheckBoxWidget clientColor = new CheckBoxWidget("Client color");

    // Icon-only particles (transparent background, alpha-cut from the source art).
    private static final Identifier[] TEXTURES = {
            Identifier.of("mre", "textures/particles/star1.png"),
            Identifier.of("mre", "textures/particles/star2.png"),
    };

    private final List<Particle> particles = new ArrayList<>();
    private long lastTime = System.currentTimeMillis();

    public WorldParticles() {
        super("WorldParticles", Type.VISUAL);
        INSTANCE = this;
        this.count.currentValue = 60f;
        this.size.currentValue = 5f;
        this.speed.currentValue = 5f;
        this.spawnArea.currentValue = 16f;
        this.mode.currentValue = 0f;
        this.clientColor.isActive = false;
        super.addWidget(this.count, this.size, this.speed, this.spawnArea, this.mode, this.clientColor);
    }

    @Override
    public void disable() {
        super.disable();
        particles.clear();
    }

    @EventSubscribe
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        long now = System.currentTimeMillis();
        float dt = Math.min(0.1f, (now - lastTime) / 1000f);
        lastTime = now;

        int target = Math.round(count.currentValue);
        Vec3d player = mc.player.getPos();

        // Maintain population.
        while (particles.size() < target) particles.add(spawn(player));
        while (particles.size() > target && !particles.isEmpty()) particles.remove(particles.size() - 1);

        Camera camera = mc.getEntityRenderDispatcher().camera;
        Vec3d cam = camera.getPos();
        MatrixStack matrices = event.getMatrix();
        float drift = speed.currentValue / 100f;
        float baseSize = size.currentValue / 10f;

        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.age += dt;
            if (p.age >= p.life) { it.remove(); continue; }

            p.x += p.vx * dt;
            p.y += (p.vy + drift) * dt;
            p.z += p.vz * dt;

            float lifeT = p.age / p.life;
            float fade = (float) Math.sin(Math.PI * lifeT); // fade in then out
            int alpha = (int) (220 * fade);
            if (alpha <= 1) continue;

            int color = clientColor.isActive
                    ? ThemeProvider.getClientColor((int) (lifeT * 200)).withAlpha(alpha).getRGB()
                    : new java.awt.Color(255, 255, 255, alpha).getRGB();

            float s = baseSize * p.scale;
            float px = (float) (p.x - cam.x);
            float py = (float) (p.y - cam.y);
            float pz = (float) (p.z - cam.z);

            matrices.push();
            matrices.translate(px, py, pz);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            Render3DUtil.drawGlowTexture(matrices.peek(), TEXTURES[p.tex], -s / 2f, -s / 2f, s, s,
                    new Vector4i(color, color, color, color), false);
            matrices.pop();
        }
    }

    private Particle spawn(Vec3d center) {
        float r = spawnArea.currentValue;
        Particle p = new Particle();
        p.x = center.x + (random.nextFloat() - 0.5f) * 2f * r;
        p.y = center.y + random.nextFloat() * 5f - 1f;
        p.z = center.z + (random.nextFloat() - 0.5f) * 2f * r;
        p.vx = (random.nextFloat() - 0.5f) * 0.2f;
        p.vy = random.nextFloat() * 0.1f;
        p.vz = (random.nextFloat() - 0.5f) * 0.2f;
        p.life = 4f + random.nextFloat() * 4f;
        p.age = random.nextFloat() * p.life; // stagger so they don't all pop at once
        p.scale = 0.7f + random.nextFloat() * 0.6f;
        p.tex = Math.max(0, Math.min(TEXTURES.length - 1, Math.round(mode.currentValue)));
        return p;
    }

    private static final class Particle {
        double x, y, z;
        float vx, vy, vz;
        float life, age, scale;
        int tex;
    }
}
