package powder.client.addon.addons.visual;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import powder.api.render.providers.ResourceProvider;
import powder.api.render.providers.ThemeProvider;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.SliderWidget;

import java.awt.Color;

/**
 * Custom block selection overlay rendered with full GLSL shaders. The vanilla
 * outline is replaced (via {@code MixinBlockOutline}) by the selected procedural
 * shader drawn over the block's faces. Only the three shader modes remain:
 * 0 = Nebula, 1 = Cobweb, 2 = Plasma.
 */
public final class BlockOverlay extends Addon {

    public static BlockOverlay INSTANCE;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    @IWidget public final SliderWidget mode = new SliderWidget(0, 2); // 0 Nebula, 1 Cobweb, 2 Plasma

    // Lazily created on first render (resource manager isn't ready during mod init).
    private ShaderProgramKey nebula;
    private ShaderProgramKey cobweb;
    private ShaderProgramKey plasma;

    private long startMillis = -1L;

    public BlockOverlay() {
        super("BlockOverlay", Type.VISUAL);
        INSTANCE = this;
        this.mode.currentValue = 0f;
        super.addWidget(this.mode);
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

        Box box = blockState.getOutlineShape(mc.world, blockPos)
                .getBoundingBox()
                .offset(blockPos.getX() - cameraX, blockPos.getY() - cameraY, blockPos.getZ() - cameraZ);

        ShaderProgram shader = RenderSystem.setShader(shaderKey());
        if (shader == null) return false;

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
        return true;
    }

    private static void addBoxFaces(BufferBuilder buffer, Matrix4f matrix, Box box) {
        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        // south / north
        quad(buffer, matrix, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ);
        quad(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ);
        // west / east
        quad(buffer, matrix, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ);
        quad(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, maxX, minY, minZ);
        // top / bottom
        quad(buffer, matrix, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ);
        quad(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, minX, minY, minZ);
    }

    private static void quad(BufferBuilder buffer, Matrix4f matrix,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4) {
        buffer.vertex(matrix, x1, y1, z1);
        buffer.vertex(matrix, x2, y2, z2);
        buffer.vertex(matrix, x3, y3, z3);
        buffer.vertex(matrix, x4, y4, z4);
    }
}
