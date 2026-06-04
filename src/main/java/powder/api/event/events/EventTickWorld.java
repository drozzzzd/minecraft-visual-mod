package powder.api.event.events;

import powder.api.event.Event;

public class EventTickWorld extends Event {

    protected int tick;

    public EventTickWorld(int tick) {
        this.tick = tick;
    }

    public int getTick() {
        return tick;
    }

}
