package powder.client.mixins.minecraft;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import powder.Powder;
import powder.client.addon.Addon;
import powder.client.addon.addons.visual.BlockOverlay;

/**
 * Replaces the vanilla block selection outline with the custom {@link BlockOverlay}
 * rendering while that addon is enabled.
 */
@Mixin(WorldRenderer.class)
public class MixinBlockOutline {

    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true, require = 0)
    private void powder$blockOverlay(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity,
                                     double cameraX, double cameraY, double cameraZ,
                                     BlockPos pos, BlockState state, int color, CallbackInfo ci) {
        Addon addon = Powder.addonSystem.getModulesByName("BlockOverlay");
        if (addon != null && addon.isEnable() && BlockOverlay.INSTANCE != null
                && BlockOverlay.INSTANCE.render(matrices, cameraX, cameraY, cameraZ, pos, state)) {
            ci.cancel();
        }
    }
}
