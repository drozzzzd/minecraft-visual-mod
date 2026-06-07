package powder.client.addon.addons.visual;

import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.SliderWidget;

/**
 * Animates chunks sliding in as they load (Rich-Modern ChunkAnimator). The source
 * drives this through {@code DynamicUniforms.ChunkSectionsValue}, which only exists
 * in the 1.21.11 render pipeline; on 1.21.4 the in-world slide needs a different
 * per-section render hook. The feature/setting is exposed here in the RENDER
 * section; the 1.21.4 render hook is pending.
 */
public final class ChunkAnimation extends Addon {

    public static ChunkAnimation INSTANCE;

    @IWidget public final SliderWidget speed = new SliderWidget(1, 20);

    public ChunkAnimation() {
        super("ChunkAnimation", Type.VISUAL);
        INSTANCE = this;
        this.speed.currentValue = 10f;
        super.addWidget(this.speed);
    }

    public float getSpeed() {
        return speed.currentValue;
    }
}
