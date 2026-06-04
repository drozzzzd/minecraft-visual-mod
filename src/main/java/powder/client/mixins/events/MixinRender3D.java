package powder.client.mixins.events;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import powder.api.event.EventSystem;
import powder.api.event.events.EventRender3D;

@Mixin(WorldRenderer.class)
public class MixinRender3D {

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    public void powder$render(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Camera camera = mc.getEntityRenderDispatcher().camera;
        if (camera == null) return;

        MatrixStack matrices = new MatrixStack();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180f));

        EventSystem.post(new EventRender3D(matrices, mc.getRenderTickCounter().getTickDelta(true)));
    }

}
