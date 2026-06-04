package powder.client.gui.screens.draggableGui;

import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import powder.api.handler.other.MouseSystem;
import powder.client.gui.screens.draggableGui.comp.Draggable;

public class DraggableGui extends Screen {

    private float blur;

    public DraggableGui() {
        super(Text.of(StringUtil.capitalize("DraggableGui")));
    }

    @Override
    public void init() {
        assert this.client != null;
        this.blur = this.client.options.getMenuBackgroundBlurrinessValue();
        this.client.options.getMenuBackgroundBlurriness().setValue(0);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for(Draggable draggable : Draggable.draggables) {
            if(draggable.isFocus && !draggable.isDrag) {
                draggable.lastX = draggable.x - mouseX;
                draggable.lastY = draggable.y - mouseY;
                draggable.isDrag = true;
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(Draggable draggable : Draggable.draggables)
            draggable.isFocus = MouseSystem.isMouseOver(mouseX, mouseY, draggable.x, draggable.y, draggable.width, draggable.height) && button == 0;

        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for(Draggable draggable : Draggable.draggables) {
            if(draggable.isDrag && draggable.isFocus) {
                draggable.x = (float) (mouseX + draggable.lastX);
                draggable.y = (float) (mouseY + draggable.lastY);
            }
        }

        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for(Draggable draggable : Draggable.draggables) {
            draggable.isDrag = false;
            draggable.isFocus = false;
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.options.getMenuBackgroundBlurriness().setValue((int) this.blur);
        super.close();
    }

}
