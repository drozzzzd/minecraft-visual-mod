package powder.client.addon.addons.visual;

import powder.api.render.providers.ThemeProvider;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.CheckBoxWidget;
import powder.client.gui.widget.widgets.SliderWidget;

import java.awt.Color;

/**
 * Recolors the first-person hand (arm + held item). The tint is applied by
 * {@code MixinHeldItemRenderer} which sets the shader color around the hand's
 * immediate draw. Color source: client color, a fixed hue, or an animated rainbow.
 */
public final class CustomHand extends Addon {

    public static CustomHand INSTANCE;

    @IWidget public final CheckBoxWidget clientColor = new CheckBoxWidget("Client color");
    @IWidget public final CheckBoxWidget rainbow      = new CheckBoxWidget("Rainbow");
    @IWidget public final SliderWidget hue            = new SliderWidget(0, 360);  // degrees, used when not client/rainbow
    @IWidget public final SliderWidget saturation     = new SliderWidget(0, 100);  // %
    @IWidget public final SliderWidget speed          = new SliderWidget(1, 50);   // /10 rainbow speed

    public CustomHand() {
        super("CustomHand", Type.VISUAL);
        INSTANCE = this;
        this.clientColor.isActive = true;
        this.rainbow.isActive = false;
        this.hue.currentValue = 0f;
        this.saturation.currentValue = 80f;
        this.speed.currentValue = 10f;
        super.addWidget(this.clientColor, this.rainbow, this.hue, this.saturation, this.speed);
    }

    /** Tint color as { r, g, b } in 0..1, used by the held-item mixin. */
    public float[] color() {
        if (clientColor.isActive) {
            Color c = new Color(ThemeProvider.getClientColor(0).getRGB());
            return new float[]{c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f};
        }

        float h;
        if (rainbow.isActive) {
            float period = 4000f / Math.max(0.1f, speed.currentValue / 10f);
            h = (System.currentTimeMillis() % (long) period) / period;
        } else {
            h = hue.currentValue / 360f;
        }
        int rgb = Color.HSBtoRGB(h, saturation.currentValue / 100f, 1f);
        return new float[]{((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f};
    }
}
