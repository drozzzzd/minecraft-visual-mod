package powder.client.handler;

import net.minecraft.client.MinecraftClient;

import org.lwjgl.glfw.GLFW;

import powder.Powder;
import powder.api.event.EventSubscribe;
import powder.api.event.events.EventKeyboard;
import powder.api.handler.other.KeyboardSystem;
import powder.client.addon.Addon;
import torovvisual.implement.screens.clickgui.ClickGui;

public class Keyboard {

    private final KeyboardSystem keyboardSystem = new KeyboardSystem();

    @EventSubscribe
    public void keyboardHandler(EventKeyboard eventKeyboard) {
        if(MinecraftClient.getInstance().currentScreen != null) return;

        for(Addon addon : Powder.addonSystem.getModules()) {
            if(this.keyboardSystem.isKeyPress(addon.getKey()))
                addon.toggleModule(addon);
        }

        if(this.keyboardSystem.isKeyPress(GLFW.GLFW_KEY_RIGHT_SHIFT))
            ClickGui.INSTANCE.openGui();
    }

}
