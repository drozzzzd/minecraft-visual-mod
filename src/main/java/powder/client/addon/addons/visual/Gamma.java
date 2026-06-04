package powder.client.addon.addons.visual;

import net.minecraft.client.MinecraftClient;

import powder.client.addon.Addon;
import powder.client.addon.Type;

public class Gamma extends Addon {

    private double gamma = 0;

    public Gamma() {
        super("Gamma", Type.VISUAL);
    }

    @Override
    public void enable() {
        this.gamma = MinecraftClient.getInstance().options.getGamma().getValue();
        MinecraftClient.getInstance().options.getGamma().setValue(1d);
        super.enable();
    }

    @Override
    public void disable() {
        MinecraftClient.getInstance().options.getGamma().setValue(this.gamma);
        super.disable();
    }

}
