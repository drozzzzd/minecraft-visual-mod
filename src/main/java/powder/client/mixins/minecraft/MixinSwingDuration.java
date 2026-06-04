package powder.client.mixins.minecraft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import powder.client.addon.addons.visual.SwingAnimation;

@Mixin(LivingEntity.class)
public class MixinSwingDuration {

    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true, require = 0)
    private void powder$getHandSwingDuration(CallbackInfoReturnable<Integer> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        SwingAnimation swing = SwingAnimation.INSTANCE;
        if (swing != null && swing.isEnable() && self == MinecraftClient.getInstance().player) {
            cir.setReturnValue((int) swing.getSwingDuration());
        }
    }

}
