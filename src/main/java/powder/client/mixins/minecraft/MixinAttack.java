package powder.client.mixins.minecraft;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import powder.api.event.EventSystem;
import powder.api.event.events.EventAttack;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinAttack {

    @Inject(method = "attackEntity", at = @At("HEAD"), require = 0)
    public void powder$attackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        EventSystem.post(new EventAttack(target));
    }

}
