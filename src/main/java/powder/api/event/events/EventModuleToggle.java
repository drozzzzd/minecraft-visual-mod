package powder.api.event.events;

import powder.api.event.Event;
import powder.client.addon.Addon;

public class EventModuleToggle extends Event {

    private final Addon addon;
    private final boolean enabled;

    public EventModuleToggle(Addon addon, boolean enabled) {
        this.addon = addon;
        this.enabled = enabled;
    }

    public Addon getModule() {
        return addon;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
