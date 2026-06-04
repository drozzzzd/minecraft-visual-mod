package powder.client.mixins.events;

import net.minecraft.client.network.ClientPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import powder.api.event.EventSystem;
import powder.api.event.events.EventTickPlayer;

@Mixin(ClientPlayerEntity.class)
public class MixinTickPlayer {

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    public void powder$tick(CallbackInfo ci) {
        EventSystem.post(new EventTickPlayer(0));
    }

}
