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

import powder.api.event.EventSubscribe;
import powder.api.event.events.EventRender2D;
import powder.api.render.providers.ResourceProvider;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.SliderWidget;

/**
 * Full-screen colour-correction post process. Copies the rendered frame to an
 * off-screen buffer and re-draws it through {@code color_correction.fsh}, applying
 * brightness / contrast / saturation / hue / gamma / temperature / vibrance.
 */
public final class ColorCorrection extends Addon {

    public static ColorCorrection INSTANCE;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    @IWidget public final SliderWidget brightness  = new SliderWidget(0, 200);  // (v-100)/100 -> -1..1
    @IWidget public final SliderWidget contrast     = new SliderWidget(0, 200);  // /100 -> 0..2
    @IWidget public final SliderWidget saturation   = new SliderWidget(0, 200);  // /100 -> 0..2
    @IWidget public final SliderWidget hue          = new SliderWidget(0, 100);  // /100 -> 0..1
    @IWidget public final SliderWidget gamma        = new SliderWidget(10, 300); // /100 -> 0.1..3
    @IWidget public final SliderWidget temperature  = new SliderWidget(0, 200);  // (v-100)/100 -> -1..1
    @IWidget public final SliderWidget vibrance     = new SliderWidget(0, 100);  // /100 -> 0..1

    private Framebuffer temp;
    private int tw, th;
    private ShaderProgramKey key;

    public ColorCorrection() {
        super("ColorCorrection", Type.VISUAL);
        INSTANCE = this;
        this.brightness.currentValue = 100f;
        this.contrast.currentValue = 100f;
        this.saturation.currentValue = 100f;
        this.hue.currentValue = 0f;
        this.gamma.currentValue = 100f;
        this.temperature.currentValue = 100f;
        this.vibrance.currentValue = 0f;
        super.addWidget(this.brightness, this.contrast, this.saturation, this.hue,
                this.gamma, this.temperature, this.vibrance);
    }

    @EventSubscribe
    public void onRender2D(EventRender2D event) {
        if (!isEnable()) return;

        int w = mc.getWindow().getFramebufferWidth();
        int h = mc.getWindow().getFramebufferHeight();
        if (w <= 0 || h <= 0 || mc.getFramebuffer() == null) return;

        try {
            if (temp == null || tw != w || th != h) {
                if (temp != null) temp.delete();
                temp = new SimpleFramebuffer(w, h, true);
                temp.setClearColor(0f, 0f, 0f, 1f);
                tw = w;
                th = h;
            }

            // Copy the rendered frame into the temp buffer, then draw it back through the shader.
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mc.getFramebuffer().fbo);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, temp.fbo);
            GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST);
            mc.getFramebuffer().beginWrite(true);

            if (key == null) {
                key = new ShaderProgramKey(ResourceProvider.getShaderIdentifier("cc/color_correction"),
                        VertexFormats.POSITION_TEXTURE, Defines.EMPTY);
            }
            ShaderProgram shader = RenderSystem.setShader(key);
            if (shader == null) return;

            RenderSystem.setShaderTexture(0, temp.getColorAttachment());
            set(shader, "Brightness", (brightness.currentValue - 100f) / 100f);
            set(shader, "Contrast", contrast.currentValue / 100f);
            set(shader, "Saturation", saturation.currentValue / 100f);
            set(shader, "Hue", hue.currentValue / 100f);
            set(shader, "Gamma", gamma.currentValue / 100f);
            set(shader, "Temperature", (temperature.currentValue - 100f) / 100f);
            set(shader, "Vibrance", vibrance.currentValue / 100f);

            Matrix4f savedProj = new Matrix4f(RenderSystem.getProjectionMatrix());
            RenderSystem.setProjectionMatrix(new Matrix4f().ortho(0f, w, h, 0f, -1f, 1f), ProjectionType.ORTHOGRAPHIC);
            Matrix4fStack modelView = RenderSystem.getModelViewStack();
            modelView.pushMatrix();
            modelView.identity();
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();

            Matrix4f m = new Matrix4f();
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            buffer.vertex(m, 0f, h, 0f).texture(0f, 0f);
            buffer.vertex(m, w, h, 0f).texture(1f, 0f);
            buffer.vertex(m, w, 0f, 0f).texture(1f, 1f);
            buffer.vertex(m, 0f, 0f, 0f).texture(0f, 1f);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            modelView.popMatrix();
            RenderSystem.setProjectionMatrix(savedProj, ProjectionType.PERSPECTIVE);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        } catch (Throwable ignored) {
        }
    }

    private static void set(ShaderProgram shader, String name, float value) {
        if (shader.getUniform(name) != null) shader.getUniform(name).set(value);
    }
}
