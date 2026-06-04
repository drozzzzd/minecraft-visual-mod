package powder;

import com.google.common.base.Suppliers;

import net.fabricmc.loader.impl.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import powder.api.event.EventSystem;
import powder.api.java.MethodSystem;
import powder.api.render.msdf.MsdfFont;
import powder.client.addon.AddonSystem;
import powder.client.config.ConfigManager;
import powder.client.handler.Keyboard;
import powder.launch.startup.ClientInitializer;
import powder.launch.startup.Protection;
import powder.launch.startup.session.Session;

import java.util.function.Supplier;

public class Powder implements ClientInitializer {

    public static final String MOD_ID = "powder", MOD_LOG = StringUtil.capitalize("\u001B[35m" + MOD_ID + "\u001B[36m");
    public static final String MOD_NAME = "Torov Visual";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_LOG);

    public static final Supplier<MsdfFont> INTER_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("inter").data("inter").build());
    public static final Session session = new Session("2025", "01");

    public static final AddonSystem addonSystem = new AddonSystem();

    @Override
    public void onInitializeClient() {
        if (!Protection.verify()) {
            LOGGER.error("Init: {}", "integrity check failed — features disabled.");
            return;
        }

        try {
            long executionClient = MethodSystem.executionTime(() -> {
                LOGGER.info("Build: {}", session);

                EventSystem.register(new Keyboard());
                LOGGER.info("Init: {}", "events were successfully initialized!");

                ConfigManager.init();
            });

            LOGGER.info("Init: {}", "initialization completed (" + executionClient + "ms)");
        } catch (Exception e) {
            LOGGER.error("Initialization failed", e);
        }
    }

}
