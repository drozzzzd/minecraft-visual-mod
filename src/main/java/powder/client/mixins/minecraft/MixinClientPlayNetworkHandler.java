package powder.client.mixins.minecraft;

import net.minecraft.client.network.ClientPlayNetworkHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import powder.client.config.ConfigManager;

/**
 * Intercepts outgoing chat so the client-side {@code .cfg} command (save / del /
 * load / list configs) is handled locally instead of being sent to the server.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void powder$cfgCommand(String content, CallbackInfo ci) {
        if (content != null && content.startsWith(".cfg")) {
            ci.cancel();
            ConfigManager.handleCommand(content);
        }
    }
}
