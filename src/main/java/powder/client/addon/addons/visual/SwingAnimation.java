package powder.client.addon.addons.visual;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.widget.IWidget;
import powder.client.gui.widget.widgets.CheckBoxWidget;
import powder.client.gui.widget.widgets.SliderWidget;

public final class SwingAnimation extends Addon {

    public static SwingAnimation INSTANCE;

    private static final String[] MODES = {
            "Обычный", "Первый", "Второй", "Третий", "Четвертый", "Пятый",
            "Swipe", "Down", "Smooth", "Smooth 2", "Power", "Feast", "Twist", "Spin", "Chop"
    };

    @IWidget public final SliderWidget animationMode = new SliderWidget(0, MODES.length - 1);
    @IWidget public final SliderWidget swingPower    = new SliderWidget(1, 10);
    @IWidget public final SliderWidget hitStrength   = new SliderWidget(5, 30); // /10
    @IWidget public final CheckBoxWidget onlySwing   = new CheckBoxWidget("Only Swing");

    public SwingAnimation() {
        super("SwingAnimation", Type.VISUAL);
        INSTANCE = this;
        this.swingPower.currentValue = 5f;
        this.hitStrength.currentValue = 10f;
        super.addWidget(this.animationMode, this.swingPower, this.hitStrength, this.onlySwing);
    }

    public float getSwingDuration() {
        return swingPower.currentValue;
    }

    private String mode() {
        int idx = (int) animationMode.currentValue;
        if (idx < 0) idx = 0;
        if (idx >= MODES.length) idx = MODES.length - 1;
        return MODES[idx];
    }

    public void renderSwordAnimation(MatrixStack matrices, float swingProgress, float equipProgress, Arm arm) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        float strength = hitStrength.currentValue / 10f;

        if (onlySwing.isActive && swingProgress == 0f) {
            matrices.translate(i * 0.56F, -0.52F, -0.72F);
            return;
        }

        float sin1 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float sin2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        float sinSmooth = (float) (Math.sin(swingProgress * Math.PI) * 0.5F);

        switch (mode()) {
            case "Обычный" -> {
                matrices.translate(0.56F, -0.52F, -0.72F);
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -60.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g * -30.0F));
            }
            case "Первый" -> {
                if (swingProgress > 0) {
                    float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    matrices.translate(0.56F, equipProgress * -0.2f - 0.5F, -0.7F);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -85.0F));
                    matrices.translate(-0.1F, 0.28F, 0.2F);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-85.0F));
                } else {
                    float n = -0.4f * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    float m = 0.2f * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float) Math.PI * 2));
                    float f1 = -0.2f * MathHelper.sin(swingProgress * (float) Math.PI);
                    matrices.translate(n, m, f1);
                    applyEquipOffset(matrices, arm, equipProgress);
                    applySwingOffset(matrices, arm, swingProgress);
                }
            }
            case "Второй" -> {
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                applyEquipOffset(matrices, arm, 0);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-60f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110f + 20f * g));
            }
            case "Третий" -> {
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                applyEquipOffset(matrices, arm, 0);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-30f * (1f - g) - 30f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110f));
            }
            case "Четвертый" -> {
                float g = MathHelper.sin(swingProgress * (float) Math.PI);
                applyEquipOffset(matrices, arm, 0);
                matrices.translate(0.1F, -0.2F, -0.3F);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-30f * g - 36f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(25f * g));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(12f));
            }
            case "Пятый" -> {
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                applyEquipOffset(matrices, arm, 0);
                matrices.translate(0.0F, -0.2F, -0.4F);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-120f * g - 3f));
            }
            case "Twist" -> {
                matrices.translate(i * 0.56F, -0.36F, -0.72F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(80 * i));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -90 * strength));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((sin1 - sin2) * 60 * i * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-30));
                matrices.translate(0, -0.1F, 0.05F);
            }
            case "Swipe" -> {
                matrices.translate(0.56F * i, -0.32F, -0.72F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60 * i));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-60 * i));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((sin2 * sin1) * -5 * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((sin2 * sin1) * -120 * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60));
            }
            case "Down" -> {
                matrices.translate(i * 0.56F, -0.32F, -0.72F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(76 * i));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * -5 * strength));
                matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(sin2 * -100 * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -155 * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100));
            }
            case "Smooth" -> {
                matrices.translate(i * 0.56F, -0.42F, -0.72F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + sin1 * -20.0F * strength)));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * sin2 * -20.0F * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -80.0F * strength));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
                matrices.translate(0, -0.1, 0);
            }
            case "Power" -> {
                matrices.translate(i * 0.56F, -0.32F, -0.72F);
                matrices.translate((-sinSmooth * sinSmooth * sin1) * i * strength, 0, 0);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(61 * i));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sin2 * strength));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((sin2 * sin1) * -5 * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((sin2 * sin1) * -30 * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sinSmooth * -60 * strength));
            }
            case "Feast" -> {
                matrices.translate(i * 0.56F, -0.32F, -0.72F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30 * i));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * 75 * i * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -45 * strength));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30 * i));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(35 * i));
            }
            case "Spin" -> {
                matrices.translate(i * 0.56F, -0.52F, -0.72F);
                float angle = (float) (System.currentTimeMillis() / 4L % 360L);
                float anim = (float) Math.sin(swingProgress * (Math.PI / 2) * 2);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle + (40.0F + strength * 5) * anim));
            }
            case "Smooth 2" -> {
                matrices.translate(i * 0.56F, -0.42F, -0.72F);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -80.0F * strength));
                matrices.translate(0, -0.1, 0);
            }
            case "Chop" -> {
                matrices.translate(0.56F * i, -0.44F, -0.72F);
                matrices.translate(0.0F, 0.33F * -0.6F, 0.0F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F * i));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sin2 * -20.0F * i * strength));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -80.0F * strength));
                matrices.translate(0.4F, 0.2F, 0.2F);
                matrices.translate(-0.5F, 0.08F, 0.0F);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(20.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(20.0F));
            }
            default -> applyDefaultSwing(matrices, arm, swingProgress, equipProgress);
        }
    }

    private void applyDefaultSwing(MatrixStack matrices, Arm arm, float swingProgress, float equipProgress) {
        applyEquipOffset(matrices, arm, equipProgress);
        applySwingOffset(matrices, arm, swingProgress);
    }

    private void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate((float) i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    private void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + f * -20.0F)));
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * g * -20.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
    }

}
