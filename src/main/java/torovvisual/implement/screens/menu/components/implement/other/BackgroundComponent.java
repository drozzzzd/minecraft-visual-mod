package torovvisual.implement.screens.menu.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import torovvisual.api.system.font.Fonts;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.render.Render2DUtil;
import torovvisual.implement.screens.menu.MenuScreen;
import torovvisual.implement.screens.menu.components.AbstractComponent;

@Setter
@Accessors(chain = true)
public class BackgroundComponent extends AbstractComponent {

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(6).softness(1).thickness(2).quality(50)
                .outlineColor(ColorUtil.getOutline()).color(ColorUtil.getMainGuiColor()).build());

        rectangle.render(ShapeProperties.create(context.getMatrices(), x + 85, y, 0.5F, height)
                .color(ColorUtil.getOutline(0.5F, 1)).build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), x + 85.5F, y + 28, width - 85.5F, 0.5F)
                .color(ColorUtil.getOutline(0.5F, 1)).build());

        Fonts.getSize(15, Fonts.Type.BOLD).drawString(matrix, "Torov", x + 8, y + 9, 0xFFD4D6E1);
        Fonts.getSize(15, Fonts.Type.BOLD).drawString(matrix, "Visual", x + 8, y + 21, ColorUtil.getClientColor());

        Fonts.getSize(16).drawString(matrix, MenuScreen.INSTANCE.getCategory().getReadableName(), x + 95, y + 13, 0xFFD4D6E1);
    }
}
