package powder.client.addon;

import powder.api.event.EventSystem;
import powder.api.event.events.EventModuleToggle;
import powder.client.addon.addons.movement.AutoSprint;
import powder.client.addon.addons.player.HitSound;
import powder.client.addon.addons.visual.BlockOverlay;
import powder.client.addon.addons.visual.ChinaHat;
import powder.client.addon.addons.visual.ColorCorrection;
import powder.client.addon.addons.visual.CustomHand;
import powder.client.addon.addons.visual.CustomSky;
import powder.client.addon.addons.visual.Gamma;
import powder.client.addon.addons.visual.HitboxCustomizer;
import powder.client.addon.addons.visual.JumpCircle;
import powder.client.addon.addons.visual.SwingAnimation;
import powder.client.addon.addons.visual.TargetESP;
import powder.client.addon.addons.visual.WorldParticles;
import torovvisual.adapter.hud.Armor;
import torovvisual.adapter.hud.BossBars;
import torovvisual.adapter.hud.Cooldowns;
import torovvisual.adapter.hud.DynamicIsland;
import torovvisual.adapter.hud.Watermark;
import torovvisual.adapter.hud.HotBar;
import torovvisual.adapter.hud.HotKeys;
import torovvisual.adapter.hud.Notifications;
import torovvisual.adapter.hud.PlayerInfo;
import torovvisual.adapter.hud.Potions;
import torovvisual.adapter.hud.ScoreBoard;
import torovvisual.adapter.hud.TargetHud;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.Widget;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddonSystem {

    public final Logic logic;

    private final List<Addon> addons = new ArrayList<>();

    public AddonSystem() {
        this.logic = new Logic();

        this.addons.add(new Watermark());
        this.addons.add(new DynamicIsland());

        // Torov Visual HUD elements
        this.addons.add(new BossBars());
        this.addons.add(new Potions());
        this.addons.add(new TargetHud());
        this.addons.add(new Armor());
        this.addons.add(new HotBar());
        this.addons.add(new HotKeys());
        this.addons.add(new Notifications());
        this.addons.add(new PlayerInfo());
        this.addons.add(new ScoreBoard());
        this.addons.add(new Cooldowns());

        this.addons.add(new Gamma());
        this.addons.add(new AutoSprint());
        this.addons.add(new HitSound());
        this.addons.add(new ChinaHat());
        this.addons.add(new CustomSky());
        this.addons.add(new JumpCircle());
        this.addons.add(new TargetESP());
        this.addons.add(new SwingAnimation());
        this.addons.add(new BlockOverlay());
        this.addons.add(new WorldParticles());
        this.addons.add(new HitboxCustomizer());
        this.addons.add(new CustomHand());
        this.addons.add(new ColorCorrection());
    }

    public List<Addon> getModules() {
        return addons;
    }

    public Addon getModulesByName(String name) {
        return this.addons.stream().filter(addon -> addon.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public List<Addon> getModulesByType(Type type) {
        return this.addons.stream().filter(addon -> addon.getModuleType().equals(type))
                .collect(Collectors.toList());
    }

    public static class Logic {

        public List<Widget> getWidgets(Addon addon) {
            return addon.widgets;
        }

        public void toggleModule(Addon addon) {
            addon.setEnable(!addon.isEnable());

            if(addon.isEnable()) addon.enable(); else addon.disable();

            EventSystem.post(new EventModuleToggle(addon, addon.isEnable()));
        }

        public boolean isSetting(Module module) {
            Class<?> clazz = module.getClass();

            for(Field field : clazz.getDeclaredFields())
                if(field.isAnnotationPresent(IWidget.class))
                    return true;

            return false;
        }

    }

}
