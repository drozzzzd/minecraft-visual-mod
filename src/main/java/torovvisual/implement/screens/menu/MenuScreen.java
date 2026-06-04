package torovvisual.implement.screens.menu;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import torovvisual.api.feature.module.ModuleCategory;
import torovvisual.api.system.animation.Animation;
import torovvisual.api.system.animation.Direction;
import torovvisual.api.system.animation.implement.DecelerateAnimation;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.api.system.sound.SoundManager;
import torovvisual.common.QuickImports;
import torovvisual.common.util.math.MathUtil;
import torovvisual.implement.screens.menu.components.AbstractComponent;
import torovvisual.implement.screens.menu.components.implement.other.*;
import torovvisual.implement.screens.menu.components.implement.settings.TextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static torovvisual.api.system.animation.Direction.BACKWARDS;
import static torovvisual.api.system.animation.Direction.FORWARDS;

@Setter
@Getter
public class MenuScreen extends Screen implements QuickImports {
    public static MenuScreen INSTANCE = new MenuScreen();
    private final List<AbstractComponent> components = new ArrayList<>();
    private final BackgroundComponent backgroundComponent = new BackgroundComponent();
    private final UserComponent userComponent = new UserComponent();
    private final SearchComponent searchComponent = new SearchComponent();
    private final CategoryContainerComponent categoryContainerComponent = new CategoryContainerComponent();
    public final Animation animation = new DecelerateAnimation().setMs(200).setValue(1);
    public ModuleCategory category = ModuleCategory.HUD;
    public int x, y, width, height;

    public void initialize() {
        animation.setDirection(FORWARDS);
        categoryContainerComponent.initializeCategoryComponents();
        components.addAll(Arrays.asList(backgroundComponent, userComponent, searchComponent, categoryContainerComponent));
    }

    public MenuScreen() {
        super(Text.of("MenuScreen"));
        initialize();
    }

    @Override
    public void tick() {
        close();
        components.forEach(AbstractComponent::tick);
        super.tick();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // No vanilla background blur / darkening when the menu opens — the menu
        // draws its own dim overlay in render(). Keeps the world sharp behind it.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        x = window.getScaledWidth() / 2 - 200;
        y = window.getScaledHeight() / 2 - 125;
        width = 400;
        height = 250;

        rectangle.render(ShapeProperties.create(context.getMatrices(), 0, 0, window.getScaledWidth(), window.getScaledHeight())
                .color(MathUtil.applyOpacity(0xFF000000, 100 * getScaleAnimation())).build());

        backgroundComponent.position(x, y).size(width, height);
        userComponent.position(x, y + height);

        searchComponent.position(x + 300, y + 6);
        categoryContainerComponent.position(x, y);

        MathUtil.scale(context.getMatrices(), x + (float) width / 2, y + (float) height / 2, getScaleAnimation(), () -> {
            components.forEach(component -> component.render(context, mouseX, mouseY, delta));
            windowManager.render(context, mouseX, mouseY, delta);
        });
        super.render(context, mouseX, mouseY, delta);
    }

    public void openGui() {
        animation.setDirection(Direction.FORWARDS);
        mc.setScreen(this);
        SoundManager.playSound(SoundManager.OPEN_GUI);
    }

    public float getScaleAnimation() {
        return animation.getOutput().floatValue();
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!windowManager.mouseClicked(mouseX, mouseY, button)) {
            components.forEach(component -> component.mouseClicked(mouseX, mouseY, button));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        components.forEach(component -> component.mouseReleased(mouseX, mouseY, button));
        windowManager.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!windowManager.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            components.forEach(component -> component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (!windowManager.mouseScrolled(mouseX, mouseY, vertical)) {
            components.forEach(component -> component.mouseScrolled(mouseX, mouseY, vertical));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && shouldCloseOnEsc()) {
            SoundManager.playSound(SoundManager.CLOSE_GUI);
            animation.setDirection(BACKWARDS);
            return true;
        }

        if (!windowManager.keyPressed(keyCode, scanCode, modifiers)) {
            components.forEach(component -> component.keyPressed(keyCode, scanCode, modifiers));
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!windowManager.charTyped(chr, modifiers)) {
            components.forEach(component -> component.charTyped(chr, modifiers));
        }
        return super.charTyped(chr, modifiers);
    }


    @Override
    public boolean shouldPause() {
        return false;
    }


    @Override
    public void close() {
        if (animation.isFinished(BACKWARDS)) {
            TextComponent.typing = false;
            super.close();
        }
    }
}
