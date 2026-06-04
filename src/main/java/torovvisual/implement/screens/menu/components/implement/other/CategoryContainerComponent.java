package torovvisual.implement.screens.menu.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.Initialization;
import torovvisual.api.feature.module.ModuleCategory;
import torovvisual.api.system.font.Fonts;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.entity.PlayerInventoryComponent;
import torovvisual.common.util.math.MathUtil;
import torovvisual.implement.screens.menu.MenuScreen;
import torovvisual.implement.screens.menu.components.AbstractComponent;
import torovvisual.implement.screens.menu.components.implement.category.CategoryComponent;
import torovvisual.implement.screens.menu.components.implement.settings.TextComponent;

import java.util.ArrayList;
import java.util.List;

@Setter
@Accessors(chain = true)
public class CategoryContainerComponent extends AbstractComponent {
    private final List<CategoryComponent> categoryComponents = new ArrayList<>();

    // Theme section: client/GUI colour swatches (red, blue, green, white).
    private static final int[] THEME_COLORS = {0xFFFF5555, 0xFF5599FF, 0xFF55FF77, 0xFFFFFFFF};
    private static final float SWATCH = 13f, SWATCH_GAP = 17f;

    private float themeRowY() {
        return y + 50 + categoryComponents.size() * 19f + 19f;
    }


    @Compile
    @Initialization
    public void initializeCategoryComponents() {
        categoryComponents.clear();
        for (ModuleCategory category : ModuleCategory.values()) {
            categoryComponents.add(new CategoryComponent(category));
        }
    }

    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float offset = 0;

        for (CategoryComponent component : categoryComponents) {
            component.x = x + 6;
            component.y = y + 50 + offset;
            component.width = 73;
            component.height = 17;
            component.render(context, mouseX, mouseY, delta);
            offset += component.height + 2;
        }

        // Theme section (a bit below the category list).
        MatrixStack matrix = context.getMatrices();
        float rowY = themeRowY();
        Fonts.getSize(12, Fonts.Type.BOLD).drawString(matrix, "Theme", x + 8, rowY - 13, 0xFFD4D6E1);
        for (int i = 0; i < THEME_COLORS.length; i++) {
            float sx = x + 8 + i * SWATCH_GAP;
            boolean selected = ColorUtil.getClientColor() == THEME_COLORS[i];
            boolean hovered = MathUtil.isHovered(mouseX, mouseY, sx, rowY, SWATCH, SWATCH);
            rectangle.render(ShapeProperties.create(matrix, sx, rowY, SWATCH, SWATCH).round(3F)
                    .thickness(selected ? 2F : (hovered ? 1.5F : 0F))
                    .outlineColor(selected ? 0xFFFFFFFF : 0x80FFFFFF)
                    .color(THEME_COLORS[i]).build());
        }
    }

    @Override
    public void tick() {
        if (TextComponent.typing || SearchComponent.typing) PlayerInventoryComponent.unPressMoveKeys();
        else PlayerInventoryComponent.updateMoveKeys();
        categoryComponents.forEach(AbstractComponent::tick);
        super.tick();
    }

    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            float rowY = themeRowY();
            for (int i = 0; i < THEME_COLORS.length; i++) {
                float sx = x + 8 + i * SWATCH_GAP;
                if (MathUtil.isHovered(mouseX, mouseY, sx, rowY, SWATCH, SWATCH)) {
                    ColorUtil.setClientColor(THEME_COLORS[i]);
                    return true;
                }
            }
        }
        categoryComponents.forEach(categoryComponent -> categoryComponent.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        categoryComponents.forEach(categoryComponent -> categoryComponent.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        categoryComponents.forEach(categoryComponent -> categoryComponent.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        categoryComponents.forEach(categoryComponent -> categoryComponent.mouseScrolled(mouseX, mouseY, amount));
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        categoryComponents.forEach(categoryComponent -> categoryComponent.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        categoryComponents.forEach(categoryComponent -> categoryComponent.charTyped(chr, modifiers));
        return super.charTyped(chr, modifiers);
    }
}
