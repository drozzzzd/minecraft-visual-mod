package powder.client.addon.addons.hud;

import org.lwjgl.glfw.GLFW;

import powder.Powder;
import powder.api.event.EventSubscribe;
import powder.api.event.events.EventRender2D;
import powder.api.render.drawing.DrawSystem;
import powder.api.render.drawing.TextSystem;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.IStyle;
import powder.client.gui.screens.draggableGui.comp.Draggable;

import java.awt.*;

public class Keybind extends Addon implements IStyle {

    public float x = 100, y = 50;
    public float width = 100, height = 18;

    private float heightOffset;

    public Keybind() {
        super("Keybind", GLFW.GLFW_KEY_N, Type.HUD);
        Draggable.draggables.add(new Draggable(this.getName(), this.x, this.y, this.width, this.height + this.heightOffset + 5));
    }

    @EventSubscribe
    public void eventRender2D(EventRender2D eventRender2D) {
        float offset = 0;

        float dragX = Draggable.draggables.get(1).x;
        float dragY = Draggable.draggables.get(1).y;

        DrawSystem.drawGradient(eventRender2D.getGraphics(), dragX, dragY, this.width, this.height, 3, 0, 0, 3, GUI_COLOR_4, GUI_COLOR_3);
        DrawSystem.drawBlur(eventRender2D.getGraphics(), dragX, dragY + this.height - 1.3f, this.width, this.heightOffset + 5, 0, 3, 3, 0, new Color(23, 23, 23).getRGB());
        TextSystem.drawText(eventRender2D.getGraphics(), this.getName(), dragX + 4, dragY + 3, 10, -1);

        for(Addon addon : Powder.addonSystem.getModules()) {
            if (addon.isEnable()) {
                String keyName = "["+GLFW.glfwGetKeyName(addon.getKey(), GLFW.glfwGetKeyScancode(addon.getKey()))+"]";

                TextSystem.drawText(eventRender2D.getGraphics(), addon.getName(), dragX + 5, dragY + this.heightOffset + 5.5f, 9, Color.LIGHT_GRAY.getRGB());
                TextSystem.drawText(eventRender2D.getGraphics(), keyName, dragX + this.width - (Powder.INTER_FONT.get().getWidth(keyName, 9) + 5),
                        dragY + this.heightOffset + 5.5f, 9, Color.LIGHT_GRAY.getRGB());

                offset += 15;
                this.heightOffset = offset;
            }
        }
    }

}
