package powder.client.mixins.minecraft;

import net.minecraft.client.gui.hud.BossBarHud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import powder.Powder;
import powder.client.addon.Addon;

/**
 * Hides the vanilla boss bar while the custom "Boss Bars" HUD addon is enabled, so
 * enabling it replaces the old bar with the new one instead of drawing both.
 */
@Mixin(BossBarHud.class)
public class MixinBossBarHud {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void powder$hideVanillaBossBar(CallbackInfo ci) {
        Addon addon = Powder.addonSystem.getModulesByName("Boss Bars");
        if (addon != null && addon.isEnable()) ci.cancel();
    }
}
