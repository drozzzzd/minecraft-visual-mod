package powder.client.addon.addons.visual;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import powder.api.event.EventSubscribe;
import powder.api.event.events.EventRender3D;
import powder.api.render.providers.ResourceProvider;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.CheckBoxWidget;
import powder.client.gui.widget.widgets.SliderWidget;

import java.awt.*;

public final class CustomSky extends Addon {

    public static CustomSky INSTANCE;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    @IWidget public final SliderWidget speed     = new SliderWidget(1, 50);   // /10  -> 0.1..5.0
    @IWidget public final SliderWidget scale     = new SliderWidget(1, 20);   //       1..20
    @IWidget public final SliderWidget intensity = new SliderWidget(1, 50);   // /1000 -> 0.001..0.050
    @IWidget public final SliderWidget alpha     = new SliderWidget(30, 100); // /100  -> 0.3..1.0
    @IWidget public final CheckBoxWidget caustic  = new CheckBoxWidget("Caustic");

    // Lazily created on first render. Building these eagerly (e.g. in a static
    // initializer) would load ResourceProvider during mod init, when the resource
    // manager is not ready yet, capturing a null and breaking all rendering.
    private ShaderProgramKey waterShader;
    private ShaderProgramKey causticShader;

    private long startMillis = -1L;

    public CustomSky() {
        super("Custom Sky", Type.VISUAL);
        INSTANCE = this;
        this.speed.currentValue = 10f;
        this.scale.currentValue = 5f;
        this.intensity.currentValue = 10f;
        this.alpha.currentValue = 100f;
        super.addWidget(this.caustic, this.speed, this.scale, this.intensity, this.alpha);
    }

    public boolean shouldReplaceSky() {
        return isEnable();
    }

    @EventSubscribe
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;
        renderSky();
    }

    private ShaderProgramKey shaderKey() {
        if (this.caustic.isActive) {
            if (causticShader == null)
                causticShader = new ShaderProgramKey(ResourceProvider.getShaderIdentifier("sky/caustic"),
                        VertexFormats.POSITION, Defines.EMPTY);
            return causticShader;
        }

        if (waterShader == null)
            waterShader = new ShaderProgramKey(ResourceProvider.getShaderIdentifier("sky/water"),
                    VertexFormats.POSITION, Defines.EMPTY);
        return waterShader;
    }

    private void renderSky() {
        if (!isEnable()) return;
        if (startMillis < 0L) startMillis = System.currentTimeMillis();

        float time = (System.currentTimeMillis() - startMillis) / 1000.0f;
        float fw = mc.getWindow().getFramebufferWidth();
        float fh = mc.getWindow().getFramebufferHeight();

        Color color = Color.getHSBColor((time * 0.03f) % 1.0f, 0.55f, 1.0f);
        float cr = color.getRed() / 255f;
        float cg = color.getGreen() / 255f;
        float cb = color.getBlue() / 255f;

        ShaderProgram shader = RenderSystem.setShader(shaderKey());
        if (shader == null) return;

        Camera cam = mc.gameRenderer.getCamera();
        float yawRad = (float) Math.toRadians(-cam.getYaw());
        float pitchRad = (float) Math.toRadians(cam.getPitch());

        shader.getUniform("uTime").set(time);
        shader.getUniform("uResolution").set(fw, fh);
        shader.getUniform("uColor").set(cr, cg, cb);
        shader.getUniform("uAlpha").set(this.alpha.currentValue / 100f);
        shader.getUniform("uSpeed").set(this.speed.currentValue / 10f);
        shader.getUniform("uScale").set(this.scale.currentValue);
        shader.getUniform("uIntensity").set(this.intensity.currentValue / 1000f);
        shader.getUniform("uCameraDir").set(yawRad, pitchRad);
        shader.getUniform("uFov").set((float) mc.options.getFov().getValue().intValue());

        Matrix4f savedProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        RenderSystem.setProjectionMatrix(new Matrix4f(), ProjectionType.ORTHOGRAPHIC);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_EQUAL);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        Matrix4f identity = new Matrix4f();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(identity, -1f, -1f, 1f);
        buffer.vertex(identity, 1f, -1f, 1f);
        buffer.vertex(identity, 1f, 1f, 1f);
        buffer.vertex(identity, -1f, 1f, 1f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.setProjectionMatrix(savedProj, ProjectionType.PERSPECTIVE);
    }

}
