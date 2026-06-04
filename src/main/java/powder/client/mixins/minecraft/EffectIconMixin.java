package powder.client.mixins.minecraft;

import net.minecraft.client.gui.hud.InGameHud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import powder.Powder;

@Mixin(InGameHud.class)
public class EffectIconMixin {
    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void cancelEffectOverlay(CallbackInfo ci) {
        if(Powder.addonSystem.getModulesByName("Potions").isEnable())
            ci.cancel();
    }
}
