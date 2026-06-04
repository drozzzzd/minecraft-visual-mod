package powder.client.addon.addons.visual;

import powder.api.render.providers.ThemeProvider;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.CheckBoxWidget;
import powder.client.gui.widget.widgets.SliderWidget;

import java.awt.Color;

/**
 * Recolors the first-person hand (arm + held item).
 *
 * <p>Two ways to render:
 * <ul>
 *   <li><b>Color</b> (default): a flat tint applied by {@code MixinHeldItemRenderer}
 *       around the hand's immediate draw (client color / hue / rainbow);</li>
 *   <li><b>Shader</b> (enable "Shader"): the hand is captured to an off-screen buffer
 *       and recolored with a procedural GLSL effect (Nebula / Starfield / Cobweb /
 *       Plasma) by {@code CustomHandShaderCapture}.</li>
 * </ul>
 */
public final class CustomHand extends Addon {

    public static CustomHand INSTANCE;

    // Color tint
    @IWidget public final CheckBoxWidget clientColor = new CheckBoxWidget("Client color");
    @IWidget public final CheckBoxWidget rainbow      = new CheckBoxWidget("Rainbow");
    @IWidget public final SliderWidget hue            = new SliderWidget(0, 360);
    @IWidget public final SliderWidget saturation     = new SliderWidget(0, 100);
    @IWidget public final SliderWidget speed          = new SliderWidget(1, 50);   // /10

    // Shader mode
    @IWidget public final CheckBoxWidget shaderEnabled = new CheckBoxWidget("Shader");
    @IWidget public final SliderWidget shaderMode       = new SliderWidget(0, 3);   // nebula/starfield/cobweb/plasma
    @IWidget public final CheckBoxWidget shaderOnly      = new CheckBoxWidget("Shader only");
    @IWidget public final SliderWidget opacity          = new SliderWidget(10, 100); // /100

    public CustomHand() {
        super("CustomHand", Type.VISUAL);
        INSTANCE = this;
        this.clientColor.isActive = true;
        this.rainbow.isActive = false;
        this.hue.currentValue = 0f;
        this.saturation.currentValue = 80f;
        this.speed.currentValue = 10f;
        this.shaderEnabled.isActive = false;
        this.shaderMode.currentValue = 0f;
        this.shaderOnly.isActive = false;
        this.opacity.currentValue = 100f;
        super.addWidget(this.clientColor, this.rainbow, this.hue, this.saturation, this.speed,
                this.shaderEnabled, this.shaderMode, this.shaderOnly, this.opacity);
    }

    /** Tint color as { r, g, b } in 0..1, used by the held-item color mixin. */
    public float[] color() {
        Color c = handShaderColor();
        return new float[]{c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f};
    }

    /** Base color (client color / fixed hue / animated rainbow) as a {@link Color}. */
    public Color handShaderColor() {
        if (clientColor.isActive) {
            return new Color(ThemeProvider.getClientColor(0).getRGB());
        }
        float h;
        if (rainbow.isActive) {
            float period = 4000f / Math.max(0.1f, speed.currentValue / 10f);
            h = (System.currentTimeMillis() % (long) period) / period;
        } else {
            h = hue.currentValue / 360f;
        }
        return Color.getHSBColor(h, saturation.currentValue / 100f, 1f);
    }

    public boolean shaderActive() {
        return isEnable() && shaderEnabled.isActive;
    }

    public int shaderModeIndex() {
        return Math.max(0, Math.min(3, Math.round(shaderMode.currentValue)));
    }

    public boolean shaderOnlyMode() {
        return shaderOnly.isActive;
    }

    public float opacity() {
        return opacity.currentValue / 100f;
    }

    public float shaderSpeed() {
        return speed.currentValue / 10f;
    }
}
