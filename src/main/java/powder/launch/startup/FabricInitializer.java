package powder.launch.startup;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

import powder.Powder;

import java.util.List;

public class FabricInitializer implements ModInitializer {

    private final List<ClientInitializer> clientInitializers = List.of(
            new Powder()
    );

    @Override
    public void onInitialize() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            for(ClientInitializer clientInitializer : clientInitializers)
                clientInitializer.onInitializeClient();
        });
    }
}
