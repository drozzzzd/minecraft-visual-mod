package powder.api.event.events;

import net.minecraft.client.util.math.MatrixStack;
import powder.api.event.Event;

public class EventRender3D extends Event {

    private final MatrixStack matrices;
    private final float tickDelta;

    public EventRender3D(MatrixStack matrices, float tickDelta) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }

    public MatrixStack getMatrix() {
        return matrices;
    }

    public float getTickDelta() {
        return tickDelta;
    }

}
