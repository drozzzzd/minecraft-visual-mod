package powder.client.mixins.minecraft;

import net.minecraft.client.gui.hud.InGameHud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import powder.Powder;
import powder.client.addon.Addon;

/**
 * Hides the vanilla hotbar (and the pieces the ported Torov Visual hotbar already
 * redraws itself) while the "HotBar" HUD addon is enabled, so only the custom
 * hotbar shows instead of both being drawn on top of each other.
 */
@Mixin(InGameHud.class)
public class MixinInGameHud {

    private static boolean powder$customHotbar() {
        Addon addon = Powder.addonSystem.getModulesByName("HotBar");
        return addon != null && addon.isEnable();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true, require = 0)
    private void powder$hideHotbar(CallbackInfo ci) {
        if (powder$customHotbar()) ci.cancel();
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true, require = 0)
    private void powder$hideExperienceLevel(CallbackInfo ci) {
        if (powder$customHotbar()) ci.cancel();
    }

    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true, require = 0)
    private void powder$hideHeldItemTooltip(CallbackInfo ci) {
        if (powder$customHotbar()) ci.cancel();
    }

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true, require = 0)
    private void powder$hideOverlayMessage(CallbackInfo ci) {
        if (powder$customHotbar()) ci.cancel();
    }
}
