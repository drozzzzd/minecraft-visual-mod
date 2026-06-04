package powder.client.addon.addons.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import powder.api.event.EventSubscribe;
import powder.api.event.events.EventRender3D;
import powder.api.math.MathUtil;
import powder.api.render.level.Render3DUtil;
import powder.api.render.providers.ThemeProvider;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.CheckBoxWidget;
import powder.client.gui.widget.widgets.SliderWidget;

/**
 * Customizes how entity hitboxes are drawn: outline width, expansion, target
 * filter, color and depth (see-through). Written fresh on Powder's 1.21.4 render
 * API (the provided Pulse source was obfuscated decompiled code); rendering goes
 * through the shared {@link Render3DUtil#drawBox} helper.
 */
public final class HitboxCustomizer extends Addon {

    public static HitboxCustomizer INSTANCE;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    @IWidget public final SliderWidget lineWidth = new SliderWidget(1, 6);     // px
    @IWidget public final SliderWidget expand     = new SliderWidget(0, 10);    // /10 blocks
    @IWidget public final CheckBoxWidget playersOnly = new CheckBoxWidget("Players only");
    @IWidget public final CheckBoxWidget self     = new CheckBoxWidget("Self");
    @IWidget public final CheckBoxWidget throughWalls = new CheckBoxWidget("Through walls");
    @IWidget public final CheckBoxWidget clientColor = new CheckBoxWidget("Client color");

    public HitboxCustomizer() {
        super("HitboxCustomizer", Type.VISUAL);
        INSTANCE = this;
        this.lineWidth.currentValue = 2f;
        this.expand.currentValue = 0f;
        this.playersOnly.isActive = true;
        this.self.isActive = false;
        this.throughWalls.isActive = true;
        this.clientColor.isActive = true;
        super.addWidget(this.lineWidth, this.expand, this.playersOnly, this.self, this.throughWalls, this.clientColor);
    }

    @EventSubscribe
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        int color = clientColor.isActive ? ThemeProvider.getClientColor(0).getRGB() : 0xFFFFFFFF;
        float grow = expand.currentValue / 10f;
        boolean depth = !throughWalls.isActive;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            if (playersOnly.isActive && !(entity instanceof PlayerEntity)) continue;
            if (entity == mc.player && !self.isActive) continue;

            // Interpolated hitbox so it tracks the entity smoothly between ticks.
            Vec3d delta = MathUtil.interpolate(entity).subtract(entity.getPos());
            Box box = entity.getBoundingBox().offset(delta).expand(grow);

            Render3DUtil.drawBox(box, color, lineWidth.currentValue, true, false, depth);
        }
    }
}
