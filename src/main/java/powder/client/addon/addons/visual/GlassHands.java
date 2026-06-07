package powder.client.addon.addons.visual;

import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.CheckBoxWidget;
import powder.client.gui.widget.widgets.SliderWidget;

/**
 * "Glass" first-person hands/items — renders the held hand with a translucent
 * tint (Rich-Modern GlassHands). The full framebuffer blur-refraction of the
 * source needs the 1.21.11 pipeline; on 1.21.4 we apply a tinted translucency in
 * {@code MixinHeldItemRenderer}, which gives the glassy look without the heavy
 * scene-capture pass. Lives in the RENDER section.
 */
public final class GlassHands extends Addon {

    public static GlassHands INSTANCE;

    @IWidget public final SliderWidget opacity = new SliderWidget(10, 100); // /100
    @IWidget public final CheckBoxWidget tint   = new CheckBoxWidget("Tint");
    @IWidget public final SliderWidget tintR    = new SliderWidget(0, 255);
    @IWidget public final SliderWidget tintG    = new SliderWidget(0, 255);
    @IWidget public final SliderWidget tintB    = new SliderWidget(0, 255);

    public GlassHands() {
        super("GlassHands", Type.VISUAL);
        INSTANCE = this;
        this.opacity.currentValue = 55f;
        this.tint.isActive = true;
        this.tintR.currentValue = 130f;
        this.tintG.currentValue = 200f;
        this.tintB.currentValue = 255f;
        super.addWidget(this.opacity, this.tint, this.tintR, this.tintG, this.tintB);
    }

    /** RGBA the hand should be tinted with (alpha = glass opacity). */
    public float[] color() {
        float a = opacity.currentValue / 100f;
        if (tint.isActive) {
            return new float[]{tintR.currentValue / 255f, tintG.currentValue / 255f, tintB.currentValue / 255f, a};
        }
        return new float[]{1f, 1f, 1f, a};
    }
}
