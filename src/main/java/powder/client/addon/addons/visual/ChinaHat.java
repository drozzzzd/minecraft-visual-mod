package powder.client.addon.addons.visual;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

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

public final class ChinaHat extends Addon {

    public static ChinaHat INSTANCE;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    @IWidget public final SliderWidget radius    = new SliderWidget(1, 20);  // /10
    @IWidget public final SliderWidget height    = new SliderWidget(3, 30);  // /10
    @IWidget public final SliderWidget spinSpeed  = new SliderWidget(0, 5);
    @IWidget public final CheckBoxWidget othersHat = new CheckBoxWidget("Others");
    @IWidget public final CheckBoxWidget selfHat   = new CheckBoxWidget("Self");
    @IWidget public final CheckBoxWidget fill      = new CheckBoxWidget("Fill");
    @IWidget public final CheckBoxWidget outline   = new CheckBoxWidget("Outline");
    @IWidget public final CheckBoxWidget pulse     = new CheckBoxWidget("Pulse");

    private static final int SEGMENTS = 48;
    private static final float OUTLINE_WIDTH = 1.5f;

    private float rotAngle = 0f;
    private float pulseTime = 0f;

    public ChinaHat() {
        super("ChinaHat", Type.VISUAL);
        INSTANCE = this;
        this.radius.currentValue = 6f;
        this.height.currentValue = 12f;
        this.spinSpeed.currentValue = 1.5f;
        this.othersHat.isActive = true;
        this.selfHat.isActive = true;
        this.fill.isActive = true;
        this.outline.isActive = true;
        this.pulse.isActive = true;
        super.addWidget(this.radius, this.height, this.spinSpeed,
                this.othersHat, this.selfHat, this.fill, this.outline, this.pulse);
    }

    @EventSubscribe
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        boolean firstPerson = mc.options.getPerspective().isFirstPerson();

        rotAngle = (rotAngle + spinSpeed.currentValue) % 360f;
        pulseTime += 0.06f;

        float pulseFactor = pulse.isActive ? 1f + 0.08f * (float) Math.sin(pulseTime) : 1f;

        MatrixStack matrices = event.getMatrix();
        Vec3d cam = mc.getEntityRenderDispatcher().camera.getPos();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!selfHat.isActive && player == mc.player) continue;
            if (!othersHat.isActive && player != mc.player) continue;
            // Your own hat can't be seen in first person (the head is at the camera),
            // so skip only the local player while in first person; others still render.
            if (firstPerson && player == mc.player) continue;

            Vec3d pos = MathUtil.interpolate(player);
            // Seat the hat right on the crown of the head (uses the live bounding-box
            // height so it follows the player's pose, e.g. sneaking) instead of a
            // fixed eye-height offset that floated above the head.
            double headY = pos.y + player.getBoundingBox().getLengthY() - 0.04;
            double cx = pos.x - cam.x, cy = headY - cam.y, cz = pos.z - cam.z;

            double r = (radius.currentValue / 10.0) * pulseFactor;
            double h = (height.currentValue / 10.0) * pulseFactor;
            double spin = Math.toRadians(rotAngle);

            int cBase  = ThemeProvider.getClientColor(0).withAlpha(55).getRGB();
            int cApex  = ThemeProvider.getClientColor(180).withAlpha(28).getRGB();
            int cLBase = ThemeProvider.getClientColor(0).getRGB();
            int cLApex = ThemeProvider.getClientColor(180).getRGB();

            matrices.push();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            MatrixStack.Entry entry = matrices.peek();

            renderCone(entry, cx, cy, cz, r, h, SEGMENTS, spin, cBase, cApex, cLBase, cLApex, cam);

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            matrices.pop();
        }
    }

    private void renderCone(MatrixStack.Entry entry, double cx, double cy, double cz,
                            double r, double h, int segs, double spin,
                            int cBase, int cApex, int cLBase, int cLApex, Vec3d cam) {
        if (fill.isActive) {
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= segs; i++) {
                double a = spin + (2 * Math.PI * i) / segs;
                buf.vertex(entry.getPositionMatrix(), (float) (cx + Math.cos(a) * r), (float) cy, (float) (cz + Math.sin(a) * r)).color(cBase);
                buf.vertex(entry.getPositionMatrix(), (float) cx, (float) (cy + h), (float) cz).color(cApex);
            }
            BufferRenderer.drawWithGlobalProgram(buf.end());

            BufferBuilder bufBase = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            bufBase.vertex(entry.getPositionMatrix(), (float) cx, (float) cy, (float) cz).color(cBase);
            for (int i = 0; i <= segs; i++) {
                double a = spin + (2 * Math.PI * i) / segs;
                bufBase.vertex(entry.getPositionMatrix(), (float) (cx + Math.cos(a) * r), (float) cy, (float) (cz + Math.sin(a) * r)).color(cBase);
            }
            BufferRenderer.drawWithGlobalProgram(bufBase.end());
        }
        if (outline.isActive) {
            RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
            RenderSystem.lineWidth(OUTLINE_WIDTH);
            BufferBuilder buf2 = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.LINES);
            for (int i = 0; i <= segs; i++) {
                double a = spin + (2 * Math.PI * i) / segs;
                float nx = (float) Math.cos(a), nz = (float) Math.sin(a);
                buf2.vertex(entry, (float) (cx + nx * r), (float) cy, (float) (cz + nz * r)).color(cLBase).normal(entry, nx, 0, nz);
            }
            BufferRenderer.drawWithGlobalProgram(buf2.end());
            for (int i = 0; i < 4; i++) {
                double a = spin + (2 * Math.PI * i) / 4;
                Render3DUtil.drawLine(
                        new Vec3d(cx + Math.cos(a) * r + cam.x, cy + cam.y, cz + Math.sin(a) * r + cam.z),
                        new Vec3d(cx + cam.x, cy + h + cam.y, cz + cam.z),
                        cLBase, cLApex, OUTLINE_WIDTH, true);
            }
        }
    }

}
