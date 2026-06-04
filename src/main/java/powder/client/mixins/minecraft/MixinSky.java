package powder.client.mixins.minecraft;

import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import powder.client.addon.addons.visual.CustomSky;

@Mixin(WorldRenderer.class)
public class MixinSky {

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true, require = 0)
    private void powder$renderSky(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
        if (CustomSky.INSTANCE != null && CustomSky.INSTANCE.shouldReplaceSky())
            ci.cancel();
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true, require = 0)
    private void powder$renderClouds(FrameGraphBuilder frameGraphBuilder, Matrix4f matrix4f, Matrix4f matrix4f2, CloudRenderMode cloudRenderMode, Vec3d vec3d, float f, int i, float f2, CallbackInfo ci) {
        if (CustomSky.INSTANCE != null && CustomSky.INSTANCE.shouldReplaceSky())
            ci.cancel();
    }

}
