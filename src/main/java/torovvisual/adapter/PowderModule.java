package torovvisual.adapter;

import powder.Powder;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.Widget;
import powder.client.gui.widget.widgets.CheckBoxWidget;
import powder.client.gui.widget.widgets.SliderWidget;
import torovvisual.api.feature.module.Module;
import torovvisual.api.feature.module.ModuleCategory;
import torovvisual.api.feature.module.setting.Setting;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps a Powder {@link Addon} as a menu {@link Module}. Toggling the module
 * toggles the underlying addon through Powder's addon system. Each addon widget
 * is exposed as a clear menu setting:
 * <ul>
 *   <li>{@link CheckBoxWidget} -> on/off toggle (keeps the widget's label);</li>
 *   <li>{@link SliderWidget} -> a button stepper named after the addon's field
 *       (e.g. {@code spinSpeed} -> "Spin Speed"); if the addon declares a matching
 *       {@code String[]} of mode names, the stepper shows those names instead of a
 *       number (e.g. TargetESP -> "Кольцо", "Призраки", ...).</li>
 * </ul>
 */
public class PowderModule extends Module {
    private final Addon addon;

    public PowderModule(Addon addon) {
        super(addon.getName(), category(addon.getModuleType()));
        this.addon = addon;
        this.state = addon.isEnable();
        setKey(addon.getKey());
        setup(buildSettings(addon).toArray(new Setting[0]));
    }

    private static List<Setting> buildSettings(Addon addon) {
        Map<Object, String> widgetNames = new IdentityHashMap<>();
        List<String[]> modeArrays = new ArrayList<>();

        for (Class<?> c = addon.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    if (Widget.class.isAssignableFrom(field.getType())) {
                        Object value = field.get(addon);
                        if (value != null) widgetNames.put(value, prettify(field.getName()));
                    } else if (Modifier.isStatic(field.getModifiers()) && field.getType() == String[].class) {
                        Object array = field.get(null);
                        if (array instanceof String[] labels && labels.length > 0) modeArrays.add(labels);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        List<Setting> settings = new ArrayList<>();
        for (Widget widget : addon.widgets) {
            if (widget instanceof CheckBoxWidget checkBox) {
                settings.add(new PowderBooleanSetting(checkBox));
            } else if (widget instanceof SliderWidget slider) {
                String name = widgetNames.getOrDefault(slider, "Value");
                settings.add(new PowderValueSetting(slider, name, matchModes(slider, modeArrays)));
            }
        }
        return settings;
    }

    /** A 0-based slider whose span matches a declared label array is treated as a mode selector. */
    private static String[] matchModes(SliderWidget slider, List<String[]> modeArrays) {
        int min = (int) slider.min;
        int max = (int) slider.max;
        if (min != 0) return null;
        for (String[] labels : modeArrays) {
            if (labels.length == max + 1) return labels;
        }
        return null;
    }

    /** "spinSpeed" -> "Spin Speed", "espMode" -> "Esp Mode". */
    private static String prettify(String fieldName) {
        String spaced = fieldName.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    private static ModuleCategory category(Type type) {
        return switch (type) {
            case HUD -> ModuleCategory.HUD;
            case VISUAL -> ModuleCategory.RENDER;
            case MOVEMENT -> ModuleCategory.MOVEMENT;
            case UTILS, DRAG -> ModuleCategory.MISC;
        };
    }

    @Override
    public boolean isState() {
        return addon.isEnable();
    }

    @Override
    public void activate() {
        if (!addon.isEnable()) Powder.addonSystem.logic.toggleModule(addon);
    }

    @Override
    public void deactivate() {
        if (addon.isEnable()) Powder.addonSystem.logic.toggleModule(addon);
    }
}
