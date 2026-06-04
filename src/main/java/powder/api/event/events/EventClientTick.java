package powder.api.event.events;

import powder.api.event.Event;

public class EventClientTick extends Event {

    protected int tick;

    public EventClientTick(int tick) {
        this.tick = tick;
    }

    public int getTick() {
        return tick;
    }

}
