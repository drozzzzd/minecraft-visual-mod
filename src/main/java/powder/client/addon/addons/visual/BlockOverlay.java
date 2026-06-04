package powder.client.addon.addons.visual;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import powder.api.render.providers.ResourceProvider;
import powder.api.render.providers.ThemeProvider;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.CheckBoxWidget;
import powder.client.gui.widget.widgets.SliderWidget;

import java.awt.Color;

/**
 * Custom block selection overlay (replaces the vanilla outline via
 * {@code MixinBlockOutline}). Four modes:
 * <ul>
 *   <li>0 = Nebula, 1 = Cobweb, 2 = Plasma — procedural GLSL shaders on the block faces;</li>
 *   <li>3 = White — the classic outline + translucent fill (white or client color).</li>
 * </ul>
 * Animation: 0 = None, 1 = Pulse, 2 = Wave (applies to every mode).
 */
public final class BlockOverlay extends Addon {

    public static BlockOverlay INSTANCE;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    @IWidget public final SliderWidget mode       = new SliderWidget(0, 3); // Nebula/Cobweb/Plasma/White
    @IWidget public final SliderWidget animation   = new SliderWidget(0, 2); // None/Pulse/Wave
    @IWidget public final SliderWidget lineWidth   = new SliderWidget(1, 5); // white mode outline
    @IWidget public final SliderWidget fillAlpha   = new SliderWidget(10, 100); // white mode fill, /100
    @IWidget public final CheckBoxWidget clientColor = new CheckBoxWidget("Client color"); // white mode tint

    // Lazily created on first render (resource manager isn't ready during mod init).
    private ShaderProgramKey nebula;
    private ShaderProgramKey cobweb;
    private ShaderProgramKey plasma;

    private long startMillis = -1L;

    public BlockOverlay() {
        super("BlockOverlay", Type.VISUAL);
        INSTANCE = this;
        this.mode.currentValue = 0f;
        this.animation.currentValue = 0f;
        this.lineWidth.currentValue = 2f;
        this.fillAlpha.currentValue = 30f;
        this.clientColor.isActive = false;
        super.addWidget(this.mode, this.animation, this.lineWidth, this.fillAlpha, this.clientColor);
    }

    private boolean isWhiteMode() {
        return Math.round(mode.currentValue) == 3;
    }

    private boolean isAnimation(int m) {
        return Math.round(animation.currentValue) == m;
    }

    private ShaderProgramKey shaderKey() {
        int m = Math.round(mode.currentValue);
        if (m == 1) {
            if (cobweb == null) cobweb = key("block/cobweb");
            return cobweb;
        }
        if (m == 2) {
            if (plasma == null) plasma = key("block/plasma");
            return plasma;
        }
        if (nebula == null) nebula = key("block/nebula");
        return nebula;
    }

    private static ShaderProgramKey key(String name) {
        return new ShaderProgramKey(ResourceProvider.getShaderIdentifier(name), VertexFormats.POSITION, Defines.EMPTY);
    }

    /** Called by {@code MixinBlockOutline} when this addon is enabled; returns true if it drew (cancel vanilla). */
    public boolean render(MatrixStack matrices, double cameraX, double cameraY, double cameraZ,
                          BlockPos blockPos, BlockState blockState) {
        if (mc.world == null || blockState.isAir()) return false;

        Box base = blockState.getOutlineShape(mc.world, blockPos)
                .getBoundingBox()
                .offset(blockPos.getX() - cameraX, blockPos.getY() - cameraY, blockPos.getZ() - cameraZ);

        Box box = applyPrimaryAnimation(base);

        if (isWhiteMode()) {
            renderClassic(matrices, box, overlayColor());
        } else {
            renderShader(matrices, box);
        }

        if (isAnimation(2)) {
            renderWave(matrices, base, overlayColor());
        }
        return true;
    }

    // ── Shader modes (Nebula / Cobweb / Plasma) ─────────────────────────────────
    private void renderShader(MatrixStack matrices, Box box) {
        ShaderProgram shader = RenderSystem.setShader(shaderKey());
        if (shader == null) return;

        if (startMillis < 0L) startMillis = System.currentTimeMillis();
        float time = (System.currentTimeMillis() - startMillis) / 1000.0f;
        Color c = new Color(ThemeProvider.getClientColor(0).getRGB());

        if (shader.getUniform("time") != null) shader.getUniform("time").set(time);
        if (shader.getUniform("screenSize") != null) {
            shader.getUniform("screenSize").set((float) mc.getWindow().getFramebufferWidth(),
                    (float) mc.getWindow().getFramebufferHeight());
        }
        if (shader.getUniform("baseColor") != null) {
            shader.getUniform("baseColor").set(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1f);
        }
        if (shader.getUniform("alpha") != null) shader.getUniform("alpha").set(1f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION);
        addBoxFaces(buffer, matrix, box);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    // ── Classic white mode (outline + fill) ─────────────────────────────────────
    private void renderClassic(MatrixStack matrices, Box box, Color color) {
        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        drawFill(matrices, box, color, fillAlpha.currentValue / 100f);
        drawOutline(matrices, box, color, 1.0f, lineWidth.currentValue);

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private void renderWave(MatrixStack matrices, Box box, Color color) {
        float progress = (System.currentTimeMillis() % 1000L) / 1000.0F;
        Box waveBox = scaleBox(box, 1.0F + progress * 0.18F);

        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        drawOutline(matrices, waveBox, color, 1.0F - progress, Math.max(1.0F, lineWidth.currentValue - 0.5F));
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private Box applyPrimaryAnimation(Box box) {
        if (!isAnimation(1)) return box;
        float progress = (float) ((Math.sin(System.currentTimeMillis() / 260.0D) + 1.0D) * 0.5D);
        return scaleBox(box, 1.0F + progress * 0.04F);
    }

    private Color overlayColor() {
        return clientColor.isActive ? new Color(ThemeProvider.getClientColor(0).getRGB()) : Color.WHITE;
    }

    private void drawOutline(MatrixStack matrices, Box box, Color color, float alpha, float width) {
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.lineWidth(width);

        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        addBoxLines(buffer, matrix, box, color, alpha);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.lineWidth(1.0F);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    private void drawFill(MatrixStack matrices, Box box, Color color, float alpha) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        addBoxQuads(buffer, matrix, box, color, alpha);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    // ── Geometry helpers ────────────────────────────────────────────────────────
    private static Box scaleBox(Box box, float scale) {
        Vec3d center = box.getCenter();
        double halfX = (box.maxX - box.minX) * 0.5D * scale;
        double halfY = (box.maxY - box.minY) * 0.5D * scale;
        double halfZ = (box.maxZ - box.minZ) * 0.5D * scale;
        return new Box(center.x - halfX, center.y - halfY, center.z - halfZ, center.x + halfX, center.y + halfY, center.z + halfZ);
    }

    private static void addBoxFaces(BufferBuilder buffer, Matrix4f matrix, Box box) {
        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        face(buffer, matrix, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ);
        face(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ);
        face(buffer, matrix, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ);
        face(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, maxX, minY, minZ);
        face(buffer, matrix, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ);
        face(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, minX, minY, minZ);
    }

    private static void face(BufferBuilder buffer, Matrix4f matrix,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4) {
        buffer.vertex(matrix, x1, y1, z1);
        buffer.vertex(matrix, x2, y2, z2);
        buffer.vertex(matrix, x3, y3, z3);
        buffer.vertex(matrix, x4, y4, z4);
    }

    private static void addBoxLines(BufferBuilder buffer, Matrix4f matrix, Box box, Color color, float alpha) {
        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        line(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, color, alpha);
        line(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, color, alpha);
        line(buffer, matrix, minX, minY, minZ, minX, minY, maxZ, color, alpha);
        line(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, color, alpha);
        line(buffer, matrix, maxX, maxY, maxZ, maxX, minY, maxZ, color, alpha);
        line(buffer, matrix, maxX, maxY, maxZ, maxX, maxY, minZ, color, alpha);
        line(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, color, alpha);
        line(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, color, alpha);
        line(buffer, matrix, minX, minY, maxZ, maxX, minY, maxZ, color, alpha);
        line(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, color, alpha);
        line(buffer, matrix, minX, maxY, maxZ, minX, minY, maxZ, color, alpha);
        line(buffer, matrix, maxX, maxY, minZ, maxX, minY, minZ, color, alpha);
    }

    private static void addBoxQuads(BufferBuilder buffer, Matrix4f matrix, Box box, Color color, float alpha) {
        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        quad(buffer, matrix, color, alpha, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ);
        quad(buffer, matrix, color, alpha, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ);
        quad(buffer, matrix, color, alpha, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ);
        quad(buffer, matrix, color, alpha, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, maxX, minY, minZ);
        quad(buffer, matrix, color, alpha, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ);
        quad(buffer, matrix, color, alpha, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, minX, minY, minZ);
    }

    private static void line(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1,
                             float x2, float y2, float z2, Color color, float alpha) {
        vertex(buffer, matrix, x1, y1, z1, color, alpha);
        vertex(buffer, matrix, x2, y2, z2, color, alpha);
    }

    private static void quad(BufferBuilder buffer, Matrix4f matrix, Color color, float alpha,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4) {
        vertex(buffer, matrix, x1, y1, z1, color, alpha);
        vertex(buffer, matrix, x2, y2, z2, color, alpha);
        vertex(buffer, matrix, x3, y3, z3, color, alpha);
        vertex(buffer, matrix, x4, y4, z4, color, alpha);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, Color color, float alpha) {
        buffer.vertex(matrix, x, y, z).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
    }
}
