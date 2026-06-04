package powder.client.mixins.minecraft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import powder.api.handler.other.MouseSystem;
import powder.api.render.drawing.DrawSystem;
import powder.client.gui.screens.draggableGui.comp.Draggable;

import java.awt.Color;

/**
 * Lets HUD elements be repositioned while the chat is open: hold left mouse over a
 * HUD element and drag. Typing in chat still works as usual.
 */
@Mixin(ChatScreen.class)
public class MixinChatScreen {

    private static boolean powder$prevDown = false;

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    public void powder$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        if (down && !powder$prevDown) {
            for (Draggable draggable : Draggable.draggables) {
                if (MouseSystem.isMouseOver(mouseX, mouseY, draggable.x, draggable.y, draggable.width, draggable.height)) {
                    draggable.isFocus = true;
                    draggable.isDrag = true;
                    draggable.lastX = draggable.x - mouseX;
                    draggable.lastY = draggable.y - mouseY;
                    break;
                }
            }
        } else if (!down) {
            for (Draggable draggable : Draggable.draggables) {
                draggable.isDrag = false;
                draggable.isFocus = false;
            }
        }

        for (Draggable draggable : Draggable.draggables) {
            if (down && draggable.isDrag && draggable.isFocus) {
                draggable.x = mouseX + draggable.lastX;
                draggable.y = mouseY + draggable.lastY;
            }
            DrawSystem.drawBorder(context, draggable.x - 1, draggable.y - 1,
                    draggable.width + 2, draggable.height + 2, 3, 3, 3, 3,
                    new Color(255, 255, 255, 90).getRGB());
        }

        powder$prevDown = down;
    }

}
