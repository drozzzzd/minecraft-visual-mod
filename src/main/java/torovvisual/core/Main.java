package torovvisual.core;

import torovvisual.api.feature.module.ModuleProvider;
import torovvisual.api.feature.module.ModuleRepository;
import torovvisual.common.util.render.ScissorManager;

/**
 * Lightweight facade that replaces the original Torov Visual client core.
 *
 * The ported Torov Visual menu was hard-wired to {@code Main} for a
 * handful of services (the module list and the scissor manager). Instead of the
 * full client we provide just those services here; the module list is built from
 * Powder's own addon system by {@link ModuleRepository}.
 */
public final class Main {
    private static final Main INSTANCE = new Main();

    private final ScissorManager scissorManager = new ScissorManager();
    private final ModuleRepository moduleRepository = new ModuleRepository();
    private final ModuleProvider moduleProvider = new ModuleProvider(moduleRepository.modules());

    public static Main getInstance() {
        return INSTANCE;
    }

    public ScissorManager getScissorManager() {
        return scissorManager;
    }

    public ModuleRepository getModuleRepository() {
        return moduleRepository;
    }

    public ModuleProvider getModuleProvider() {
        return moduleProvider;
    }
}
