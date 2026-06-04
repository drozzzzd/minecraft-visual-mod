package powder.client.addon.addons.player;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import powder.api.event.EventSubscribe;
import powder.api.event.events.EventAttack;
import powder.api.game.SoundManager;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.SliderWidget;

public final class HitSound extends Addon {

    public static HitSound INSTANCE;

    // 0 = Bell, 1 = Bubble, 2 = Hit1, 3 = Hit2, 4 = Bonk
    @IWidget
    public final SliderWidget soundType = new SliderWidget(0, 4);

    // Volume in percent (/100 -> 0.0 .. 2.0). Adjust to make hits louder/quieter.
    @IWidget
    public final SliderWidget volume = new SliderWidget(0, 200);

    private static final Identifier[] SOUNDS = {
            SoundManager.BELL,
            SoundManager.BUBBLE,
            SoundManager.HIT1,
            SoundManager.HIT2,
            SoundManager.BONK,
    };

    public HitSound() {
        super("HitSound", Type.UTILS);
        INSTANCE = this;
        this.soundType.currentValue = 0f;
        this.volume.currentValue = 100f;
        super.addWidget(this.soundType, this.volume);
    }

    @EventSubscribe
    public void onAttack(EventAttack event) {
        if (!(event.getTarget() instanceof LivingEntity)) return;

        float vol = volume.currentValue / 100f;
        if (vol <= 0f) return;

        int index = Math.max(0, Math.min(SOUNDS.length - 1, Math.round(soundType.currentValue)));
        SoundManager.playSound(SOUNDS[index], vol, 1f);
    }
}
