package powder.client.mixins.minecraft;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import powder.client.addon.addons.visual.CustomHandShaderCapture;

/**
 * Wraps the first-person hand render so {@code CustomHand}'s shader mode can capture
 * the hand to an off-screen buffer and composite it back through a GLSL effect.
 */
@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "renderHand", at = @At("HEAD"), require = 0)
    private void powder$beginHandCapture(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci) {
        CustomHandShaderCapture.beginCapture();
    }

    @Inject(method = "renderHand", at = @At("RETURN"), require = 0)
    private void powder$endHandCapture(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci) {
        CustomHandShaderCapture.endCapture();
    }
}
