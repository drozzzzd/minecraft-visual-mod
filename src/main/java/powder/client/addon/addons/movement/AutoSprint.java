package powder.client.addon.addons.movement;

import net.minecraft.client.MinecraftClient;

import powder.api.event.EventSubscribe;
import powder.api.event.events.EventTickPlayer;
import powder.client.addon.Addon;
import powder.client.addon.Type;

public final class AutoSprint extends Addon {

    public static AutoSprint INSTANCE;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public AutoSprint() {
        super("AutoSprint", Type.UTILS);
        INSTANCE = this;
    }

    @EventSubscribe
    public void onTick(EventTickPlayer event) {
        if (mc.player != null) {
            mc.options.sprintKey.setPressed(true);
        }
    }

}
