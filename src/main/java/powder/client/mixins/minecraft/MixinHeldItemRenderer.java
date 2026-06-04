package powder.client.mixins.minecraft;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import powder.client.addon.addons.visual.SwingAnimation;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {

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
