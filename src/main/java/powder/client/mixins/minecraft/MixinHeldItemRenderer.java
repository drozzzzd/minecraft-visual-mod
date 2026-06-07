package powder.client.mixins.minecraft;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import powder.client.addon.addons.visual.CustomHand;
import powder.client.addon.addons.visual.GlassHands;
import powder.client.addon.addons.visual.SwingAnimation;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {

    private static final String RENDER_ITEM = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V";

    @Inject(method = RENDER_ITEM, at = @At("HEAD"), require = 0)
    private void powder$handColorBegin(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers,
                                       ClientPlayerEntity player, int light, CallbackInfo ci) {
        CustomHand hand = CustomHand.INSTANCE;
        if (hand != null && hand.isEnable() && !hand.shaderEnabled.isActive) {
            float[] c = hand.color();
            RenderSystem.setShaderColor(c[0], c[1], c[2], 1f);
        }
        GlassHands glass = GlassHands.INSTANCE;
        if (glass != null && glass.isEnable()) {
            float[] g = glass.color();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(g[0], g[1], g[2], g[3]);
        }
    }

    @Inject(method = RENDER_ITEM, at = @At("RETURN"), require = 0)
    private void powder$handColorEnd(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers,
                                     ClientPlayerEntity player, int light, CallbackInfo ci) {
        CustomHand hand = CustomHand.INSTANCE;
        if (hand != null && hand.isEnable() && !hand.shaderEnabled.isActive) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
        GlassHands glass = GlassHands.INSTANCE;
        if (glass != null && glass.isEnable()) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    @Inject(method = "applyEquipOffset", at = @At("HEAD"), cancellable = true, require = 0)
    private void powder$applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress, CallbackInfo ci) {
        SwingAnimation swing = SwingAnimation.INSTANCE;
        if (swing != null && swing.isEnable()) ci.cancel();
    }

    @Inject(method = "applySwingOffset", at = @At("HEAD"), cancellable = true, require = 0)
    private void powder$applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress, CallbackInfo ci) {
        SwingAnimation swing = SwingAnimation.INSTANCE;
        if (swing != null && swing.isEnable()) {
            swing.renderSwordAnimation(matrices, swingProgress, 0f, arm);
            ci.cancel();
        }
    }

}
