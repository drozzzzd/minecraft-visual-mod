package sweetie.evaware.client.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import sweetie.evaware.api.module.Category;
import sweetie.evaware.api.module.Module;
import sweetie.evaware.api.module.ModuleRegister;
import sweetie.evaware.api.module.setting.ModeSetting;
import sweetie.evaware.api.module.setting.SliderSetting;
import sweetie.evaware.api.system.files.FileUtil;
import sweetie.evaware.api.utils.color.UIColors;

import java.awt.*;

@ModuleRegister(name = "ShaderFog", category = Category.RENDER)
public class ShaderFogModule extends Module {
    @Getter private static final ShaderFogModule instance = new ShaderFogModule();

    private final ModeSetting mode = new ModeSetting("Режим")
            .value("Water").values("Water", "Caustic");

    private final SliderSetting speed = new SliderSetting("Скорость")
            .value(1.0f).range(0.1f, 5.0f).step(0.1f);

    private final SliderSetting scale = new SliderSetting("Размер")
            .value(5.0f).range(1.0f, 20.0f).step(0.5f);

    private final SliderSetting intensity = new SliderSetting("Интенсивность")
            .value(0.01f).range(0.001f, 0.05f).step(0.001f);

    private final SliderSetting alpha = new SliderSetting("Прозрачность")
            .value(1.0f).range(0.3f, 1.0f).step(0.05f);

    private static final ShaderProgramKey WATER_SHADER = new ShaderProgramKey(
            FileUtil.getShader("post/sky/water"), VertexFormats.POSITION, Defines.EMPTY);

    private static final ShaderProgramKey CAUSTIC_SHADER = new ShaderProgramKey(
            FileUtil.getShader("post/sky/caustic"), VertexFormats.POSITION, Defines.EMPTY);

    private long startMillis = -1;

    public ShaderFogModule() {
        addSettings(mode, speed, scale, intensity, alpha);
    }

    @Override
    public void onEvent() {
        sweetie.evaware.api.event.EventListener renderEvent = sweetie.evaware.api.event.events.render.Render3DEvent.getInstance().subscribe(
            new sweetie.evaware.api.event.Listener<>(event -> {
                if (mc.player == null || mc.world == null) return;
                renderSkyShader();
            })
        );
        addEvents(renderEvent);
    }

    @Override
    public void onDisable() {
        startMillis = -1;
    }

    public void renderSkyShader() {
        if (!isEnabled()) return;
        if (startMillis < 0) startMillis = System.currentTimeMillis();

        float time = (System.currentTimeMillis() - startMillis) / 1000.0f;
        float fw = mc.getWindow().getFramebufferWidth();
        float fh = mc.getWindow().getFramebufferHeight();

        Color themeColor = UIColors.primary();
        float cr = themeColor.getRed() / 255f;
        float cg = themeColor.getGreen() / 255f;
        float cb = themeColor.getBlue() / 255f;

        ShaderProgramKey key = mode.is("Caustic") ? CAUSTIC_SHADER : WATER_SHADER;

        ShaderProgram shader = RenderSystem.setShader(key);
        if (shader == null) return;

        shader.getUniform("uTime").set(time);
        shader.getUniform("uResolution").set(fw, fh);
        shader.getUniform("uColor").set(cr, cg, cb);
        shader.getUniform("uAlpha").set(alpha.getValue());
        shader.getUniform("uSpeed").set(speed.getValue());
        shader.getUniform("uScale").set(scale.getValue());
        shader.getUniform("uIntensity").set(intensity.getValue());

        net.minecraft.client.render.Camera cam = mc.gameRenderer.getCamera();
        float yawRad = (float) Math.toRadians(-cam.getYaw());
        float pitchRad = (float) Math.toRadians(cam.getPitch());
        shader.getUniform("uCameraDir").set(yawRad, pitchRad);
        shader.getUniform("uFov").set((float) mc.options.getFov().getValue().intValue());

        Matrix4f savedProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        RenderSystem.setProjectionMatrix(new Matrix4f(), com.mojang.blaze3d.systems.ProjectionType.ORTHOGRAPHIC);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(org.lwjgl.opengl.GL11.GL_EQUAL);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        Matrix4f identity = new Matrix4f();
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buf.vertex(identity, -1f, -1f, 1f);
        buf.vertex(identity, 1f, -1f, 1f);
        buf.vertex(identity, 1f, 1f, 1f);
        buf.vertex(identity, -1f, 1f, 1f);
        BufferRenderer.drawWithGlobalProgram(buf.end());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthFunc(org.lwjgl.opengl.GL11.GL_LEQUAL);
        RenderSystem.setProjectionMatrix(savedProj, com.mojang.blaze3d.systems.ProjectionType.PERSPECTIVE);

        AmbienceModule ambience = AmbienceModule.getInstance();
        if (ambience != null && ambience.isEnabled() && ambience.applyBackgroundColor()) {
            float fogDensity = ambience.getFogSkyDensity();
            Color fogCol = ambience.getFogColor();
            if (fogCol != null && fogDensity > 0f) {
                Matrix4f savedProj2 = new Matrix4f(RenderSystem.getProjectionMatrix());
                RenderSystem.setProjectionMatrix(new Matrix4f(), com.mojang.blaze3d.systems.ProjectionType.ORTHOGRAPHIC);
                RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.POSITION_COLOR);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc(org.lwjgl.opengl.GL11.GL_EQUAL);
                RenderSystem.depthMask(false);
                RenderSystem.disableCull();

                Matrix4f fogMat = new Matrix4f();
                int r = fogCol.getRed(), g = fogCol.getGreen(), b = fogCol.getBlue();
                int a = (int)(fogDensity * 255);
                BufferBuilder fogBuf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                fogBuf.vertex(fogMat, -1f, -1f, 1f).color(r, g, b, a);
                fogBuf.vertex(fogMat, 1f, -1f, 1f).color(r, g, b, a);
                fogBuf.vertex(fogMat, 1f, 1f, 1f).color(r, g, b, a);
                fogBuf.vertex(fogMat, -1f, 1f, 1f).color(r, g, b, a);
                BufferRenderer.drawWithGlobalProgram(fogBuf.end());

                RenderSystem.depthMask(true);
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                RenderSystem.setProjectionMatrix(savedProj2, com.mojang.blaze3d.systems.ProjectionType.PERSPECTIVE);
            }
        }
    }

    public void renderSky(MatrixStack matrices) {
        renderSkyShader();
    }

    public boolean shouldCancelClouds() {
        return isEnabled();
    }
}
