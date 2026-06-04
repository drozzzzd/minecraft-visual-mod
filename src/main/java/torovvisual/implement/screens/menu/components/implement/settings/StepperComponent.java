package torovvisual.implement.screens.menu.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import torovvisual.api.feature.module.setting.implement.ValueSetting;
import torovvisual.api.system.font.Fonts;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.math.MathUtil;
import torovvisual.common.util.other.StringUtil;

import static torovvisual.api.system.font.Fonts.Type.BOLD;

/**
 * Numeric / mode setting rendered as a clear button stepper ({@code <  value  >})
 * instead of a draggable bar. When the setting carries named options the value is
 * shown as the mode name.
 */
public class StepperComponent extends AbstractSettingComponent {
    private final ValueSetting setting;

    private float leftArrowX, rightArrowX;

    public StepperComponent(ValueSetting setting) {
        super(setting);
        this.setting = setting;
    }

    private String display() {
        String[] options = setting.getOptions();
        int value = (int) setting.getValue();
        if (options != null) {
            int index = value - (int) setting.getMin();
            if (index >= 0 && index < options.length) return options[index];
        }
        return String.valueOf(value);
    }

    private void step(int delta) {
        int min = (int) setting.getMin();
        int max = (int) setting.getMax();
        int value = MathHelper.clamp((int) setting.getValue() + delta, min, max);
        setting.setValue((float) value);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();

        String wrapped = StringUtil.wrap(setting.getDescription(), 70, 12);
        height = (int) (18 + Fonts.getSize(12).getStringHeight(wrapped) / 3);

        Fonts.getSize(14, BOLD).drawString(matrix, setting.getName(), x + 9, y + 6, 0xFFD4D6E1);
        Fonts.getSize(12).drawString(matrix, wrapped, x + 9, y + 15, 0xFF878894);

        String value = display();
        float rightX = x + width - 14;
        float valueWidth = Fonts.getSize(12, BOLD).getStringWidth(value);
        float valueX = rightX - 6 - valueWidth;
        float leftX = valueX - 11;

        boolean leftHover = MathUtil.isHovered(mouseX, mouseY, leftX - 2, y + 4, 9, 11);
        boolean rightHover = MathUtil.isHovered(mouseX, mouseY, rightX - 2, y + 4, 9, 11);

        Fonts.getSize(15, BOLD).drawString(matrix, "<", leftX, y + 6, leftHover ? ColorUtil.getClientColor() : 0xFFB9BBC6);
        // The value uses a smaller font than the arrows; since glyphs are top-anchored,
        // nudge it down so it sits vertically centered on the arrow line.
        Fonts.getSize(12, BOLD).drawString(matrix, value, valueX, y + 7.5F, ColorUtil.getClientColor());
        Fonts.getSize(15, BOLD).drawString(matrix, ">", rightX, y + 6, rightHover ? ColorUtil.getClientColor() : 0xFFB9BBC6);

        this.leftArrowX = leftX;
        this.rightArrowX = rightX;
    }

    @Compile
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (MathUtil.isHovered(mouseX, mouseY, leftArrowX - 2, y + 4, 9, 11)) step(-1);
            else if (MathUtil.isHovered(mouseX, mouseY, rightArrowX - 2, y + 4, 9, 11)) step(1);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
