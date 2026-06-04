package sweetie.evaware.inject.render;

import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sweetie.evaware.client.features.modules.render.ShaderFogModule;

@Mixin(WorldRenderer.class)
public class MixinLevelRenderer {

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void onRenderSky(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
        ShaderFogModule module = ShaderFogModule.getInstance();
        if (module != null && module.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void onRenderClouds(FrameGraphBuilder frameGraphBuilder, Matrix4f matrix4f, Matrix4f matrix4f2, CloudRenderMode cloudRenderMode, Vec3d vec3d, float f, int i, float f2, CallbackInfo ci) {
        if (ShaderFogModule.getInstance() != null && ShaderFogModule.getInstance().isEnabled()) {
            ci.cancel();
        }
    }
}
