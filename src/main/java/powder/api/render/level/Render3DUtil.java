package powder.api.render.level;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;
import org.joml.Vector4i;

/**
 * Minimal world-space rendering helpers for Torov Visual modules.
 * All helpers build a camera-relative, view-rotated matrix (origin at camera),
 * matching the convention used by {@code EventRender3D#getMatrix()}.
 */
public final class Render3DUtil {

    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    private static Camera camera() {
        return mc().getEntityRenderDispatcher().camera;
    }

    private static MatrixStack cameraStack(Camera camera) {
        MatrixStack matrices = new MatrixStack();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180f));
        return matrices;
    }

    // ── Glow billboard ─────────────────────────────────────────────────────────
    public static void drawGlowTexture(MatrixStack.Entry entry, Identifier texture,
                                       float x, float y, float width, float height,
                                       Vector4i color, boolean depth) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        if (depth) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, texture);

        Matrix4f matrix = entry.getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix, x, y, 0f).texture(0f, 0f).color(color.x);
        buffer.vertex(matrix, x, y + height, 0f).texture(0f, 1f).color(color.y);
        buffer.vertex(matrix, x + width, y + height, 0f).texture(1f, 1f).color(color.z);
        buffer.vertex(matrix, x + width, y, 0f).texture(1f, 0f).color(color.w);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    // ── Line ────────────────────────────────────────────────────────────────────
    public static void drawLine(Vec3d start, Vec3d end, int colorStart, int colorEnd,
                                float width, boolean depth) {
        Camera camera = camera();
        Vec3d cam = camera.getPos();
        MatrixStack matrices = cameraStack(camera);
        MatrixStack.Entry entry = matrices.peek();

        float sx = (float) (start.x - cam.x), sy = (float) (start.y - cam.y), sz = (float) (start.z - cam.z);
        float ex = (float) (end.x - cam.x), ey = (float) (end.y - cam.y), ez = (float) (end.z - cam.z);

        float nx = ex - sx, ny = ey - sy, nz = ez - sz;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 1.0e-4f) { nx /= len; ny /= len; nz /= len; } else { nx = 0; ny = 1; nz = 0; }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(width);
        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.LINES);
        buffer.vertex(entry, sx, sy, sz).color(colorStart).normal(entry, nx, ny, nz);
        buffer.vertex(entry, ex, ey, ez).color(colorEnd).normal(entry, nx, ny, nz);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    // ── Box outline ───────────────────────────────────────────────────────────
    public static void drawBox(Box box, int color, float width,
                               boolean outline, boolean sides, boolean depth) {
        Camera camera = camera();
        Vec3d cam = camera.getPos();
        MatrixStack matrices = cameraStack(camera);
        MatrixStack.Entry entry = matrices.peek();

        float x1 = (float) (box.minX - cam.x), y1 = (float) (box.minY - cam.y), z1 = (float) (box.minZ - cam.z);
        float x2 = (float) (box.maxX - cam.x), y2 = (float) (box.maxY - cam.y), z2 = (float) (box.maxZ - cam.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(width);
        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);

        BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.LINES);
        // bottom
        edge(b, entry, x1, y1, z1, x2, y1, z1, color);
        edge(b, entry, x2, y1, z1, x2, y1, z2, color);
        edge(b, entry, x2, y1, z2, x1, y1, z2, color);
        edge(b, entry, x1, y1, z2, x1, y1, z1, color);
        // top
        edge(b, entry, x1, y2, z1, x2, y2, z1, color);
        edge(b, entry, x2, y2, z1, x2, y2, z2, color);
        edge(b, entry, x2, y2, z2, x1, y2, z2, color);
        edge(b, entry, x1, y2, z2, x1, y2, z1, color);
        // verticals
        edge(b, entry, x1, y1, z1, x1, y2, z1, color);
        edge(b, entry, x2, y1, z1, x2, y2, z1, color);
        edge(b, entry, x2, y1, z2, x2, y2, z2, color);
        edge(b, entry, x1, y1, z2, x1, y2, z2, color);
        BufferRenderer.drawWithGlobalProgram(b.end());
    }

    private static void edge(BufferBuilder b, MatrixStack.Entry entry,
                             float ax, float ay, float az, float bx, float by, float bz, int color) {
        float nx = bx - ax, ny = by - ay, nz = bz - az;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 1.0e-4f) { nx /= len; ny /= len; nz /= len; } else { nx = 0; ny = 1; nz = 0; }
        b.vertex(entry, ax, ay, az).color(color).normal(entry, nx, ny, nz);
        b.vertex(entry, bx, by, bz).color(color).normal(entry, nx, ny, nz);
    }

}
