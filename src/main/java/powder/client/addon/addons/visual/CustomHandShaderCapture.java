package powder.client.addon.addons.visual;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL30;

import powder.api.render.providers.ResourceProvider;

import java.awt.Color;

/**
 * Captures the first-person hand into an off-screen buffer (redirecting rendering
 * during {@code GameRenderer.renderHand}) and then composites it back through the
 * {@code hand_shader} GLSL effect. Adapted from the Pulse implementation to
 * Powder's shader system. Everything is guarded — on any failure it falls back to
 * normal hand rendering.
 */
public final class CustomHandShaderCapture {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static Framebuffer handFbo = null;
    private static int fbW = 0;
    private static int fbH = 0;
    private static boolean active = false;
    private static long startMillis = -1L;
    private static ShaderProgramKey shaderKey;

    private CustomHandShaderCapture() {
    }

    public static void beginCapture() {
        if (mc.currentScreen != null) {
            reset();
            return;
        }
        CustomHand hand = CustomHand.INSTANCE;
        if (hand == null || !hand.shaderActive() || active) return;

        try {
            int w = mc.getWindow().getFramebufferWidth();
            int h = mc.getWindow().getFramebufferHeight();
            if (handFbo == null || fbW != w || fbH != h) {
                if (handFbo != null) handFbo.delete();
                handFbo = new SimpleFramebuffer(w, h, true);
                handFbo.setClearColor(0f, 0f, 0f, 0f);
                fbW = w;
                fbH = h;
            }

            // Copy the main scene depth so the captured hand depth-tests correctly,
            // then clear only the colour and redirect rendering into our buffer.
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mc.getFramebuffer().fbo);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, handFbo.fbo);
            GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL30.GL_DEPTH_BUFFER_BIT, GL30.GL_NEAREST);

            handFbo.beginWrite(false);
            RenderSystem.clearColor(0f, 0f, 0f, 0f);
            RenderSystem.clear(GL30.GL_COLOR_BUFFER_BIT);
            active = true;
        } catch (Throwable t) {
            reset();
        }
    }

    public static void endCapture() {
        if (!active) return;
        active = false;
        try {
            mc.getFramebuffer().beginWrite(true);
            CustomHand hand = CustomHand.INSTANCE;
            if (hand != null && hand.shaderActive()) {
                composite(hand);
            }
        } catch (Throwable ignored) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    private static void composite(CustomHand hand) {
        if (handFbo == null) return;
        if (shaderKey == null) {
            shaderKey = new ShaderProgramKey(ResourceProvider.getShaderIdentifier("hand/hand_shader"),
                    VertexFormats.POSITION_TEXTURE, Defines.EMPTY);
        }
        ShaderProgram shader = RenderSystem.setShader(shaderKey);
        if (shader == null) return;

        if (startMillis < 0L) startMillis = System.currentTimeMillis();
        float time = (System.currentTimeMillis() - startMillis) / 1000.0f * hand.shaderSpeed();
        Color c = hand.handShaderColor();
        int w = fbW;
        int h = fbH;

        RenderSystem.setShaderTexture(0, handFbo.getColorAttachment());

        if (shader.getUniform("time") != null) shader.getUniform("time").set(time);
        if (shader.getUniform("screenSize") != null) shader.getUniform("screenSize").set((float) w, (float) h);
        if (shader.getUniform("baseColor") != null)
            shader.getUniform("baseColor").set(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1f);
        if (shader.getUniform("alpha") != null) shader.getUniform("alpha").set(hand.opacity());
        if (shader.getUniform("shaderMode") != null) shader.getUniform("shaderMode").set(hand.shaderModeIndex());
        if (shader.getUniform("shaderOnlyMode") != null) shader.getUniform("shaderOnlyMode").set(hand.shaderOnlyMode() ? 1 : 0);

        Matrix4f savedProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        RenderSystem.setProjectionMatrix(new Matrix4f().ortho(0f, w, h, 0f, -1f, 1f), ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = new Matrix4f();
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, 0f, h, 0f).texture(0f, 1f);
        buffer.vertex(matrix, w, h, 0f).texture(1f, 1f);
        buffer.vertex(matrix, w, 0f, 0f).texture(1f, 0f);
        buffer.vertex(matrix, 0f, 0f, 0f).texture(0f, 0f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        modelView.popMatrix();
        RenderSystem.setProjectionMatrix(savedProj, ProjectionType.PERSPECTIVE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void reset() {
        active = false;
    }

    public static void dispose() {
        if (handFbo != null) {
            handFbo.delete();
            handFbo = null;
        }
        fbW = 0;
        fbH = 0;
        active = false;
    }
}
