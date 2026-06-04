package torovvisual.api.feature.module;

import powder.Powder;
import powder.client.addon.Addon;
import torovvisual.adapter.PowderModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the menu's module list from Powder's addon system. Each Powder
 * {@link Addon} is wrapped in a {@link PowderModule} so the ported menu can
 * display and toggle it. The list is built once and cached so the menu's
 * component identity (and toggle state) stays stable across renders.
 */
public class ModuleRepository {
    private final List<Module> modules = new ArrayList<>();

    public ModuleRepository() {
        setup();
    }

    private void setup() {
        for (Addon addon : Powder.addonSystem.getModules()) {
            modules.add(new PowderModule(addon));
        }
    }

    public List<Module> modules() {
        return modules;
    }
}
