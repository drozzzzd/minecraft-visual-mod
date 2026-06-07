package powder.client.addon.addons.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import powder.api.event.EventSubscribe;
import powder.api.event.events.EventModuleToggle;
import powder.api.game.SoundManager;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.SliderWidget;

/**
 * Plays a short click whenever a feature is toggled on/off (the Rich-Modern
 * "ClientSounds"). Two sound sets ("New" / "Old") selectable via the stepper, and
 * a volume slider. Lives in the RENDER section.
 */
public final class Sounds extends Addon {

    public static Sounds INSTANCE;

    /** Stepper labels — matched by PowderModule to {@link #soundType} (0=New, 1=Old). */
    public static final String[] soundTypeModes = {"New", "Old"};

    @IWidget public final SliderWidget soundType = new SliderWidget(0, 1);
    @IWidget public final SliderWidget volume    = new SliderWidget(0, 200); // /100

    // New set
    private static final Identifier NEW_ON  = SoundManager.BELL;
    private static final Identifier NEW_OFF = SoundManager.BUBBLE;
    // Old set
    private static final Identifier OLD_ON  = SoundManager.HIT1;
    private static final Identifier OLD_OFF = SoundManager.HIT2;

    public Sounds() {
        super("Sounds", Type.VISUAL);
        INSTANCE = this;
        this.soundType.currentValue = 0f;
        this.volume.currentValue = 100f;
        super.addWidget(this.soundType, this.volume);
    }

    @EventSubscribe
    public void onModuleToggle(EventModuleToggle event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (event.getModule() == this) return;

        float vol = volume.currentValue / 100f;
        if (vol <= 0f) return;

        boolean old = Math.round(soundType.currentValue) == 1;
        Identifier sound = event.isEnabled()
                ? (old ? OLD_ON : NEW_ON)
                : (old ? OLD_OFF : NEW_OFF);

        SoundManager.playSound(sound, vol, 1f);
    }
}
