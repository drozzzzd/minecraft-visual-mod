package powder.api.event.events;

import net.minecraft.entity.Entity;
import powder.api.event.Event;

public class EventAttack extends Event {

    private final Entity target;

    public EventAttack(Entity target) {
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }

}
