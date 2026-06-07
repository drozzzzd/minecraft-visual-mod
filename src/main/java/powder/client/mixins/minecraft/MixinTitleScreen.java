package powder.client.mixins.minecraft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import torovvisual.implement.screens.loading.LoadingScreen;
import torovvisual.implement.screens.menu.mainmenu.MainMenu;

/**
 * Replaces the vanilla title screen with the custom Torov Visual main menu.
 * Fires whenever the game opens a {@link TitleScreen} (startup, disconnect,
 * "back to title"), so the branded menu is shown every time.
 */
@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    private static boolean torov$loadingShown = false;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void torov$replaceWithCustomMenu(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!MainMenu.ENABLED) return;
        if (client.currentScreen instanceof MainMenu || client.currentScreen instanceof LoadingScreen) return;

        // First title screen of the session -> branded loading splash, then the menu.
        if (!torov$loadingShown) {
            torov$loadingShown = true;
            client.setScreen(new LoadingScreen());
        } else {
            client.setScreen(new MainMenu());
        }
        ci.cancel();
    }
}
