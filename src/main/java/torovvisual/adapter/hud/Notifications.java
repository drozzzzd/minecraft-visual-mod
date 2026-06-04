package torovvisual.adapter.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import torovvisual.api.system.animation.Animation;
import torovvisual.api.system.animation.Direction;
import torovvisual.api.system.animation.implement.DecelerateAnimation;
import torovvisual.api.system.font.FontRenderer;
import torovvisual.api.system.font.Fonts;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.api.system.sound.SoundManager;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.math.MathUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Notifications extends HudElement {
    public static Notifications INSTANCE;

    private final List<Notification> list = new ArrayList<>();

    public Notifications() {
        super("Notifications", 0, 50, 100, 15);
        INSTANCE = this;
    }

    @Override
    public void tick() {
        list.forEach(notif -> {
            if (System.currentTimeMillis() > notif.removeTime) notif.anim.setDirection(Direction.BACKWARDS);
        });
        list.removeIf(notif -> notif.anim.isFinished(Direction.BACKWARDS));
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();
        FontRenderer font = Fonts.getSize(12, Fonts.Type.DEFAULT);

        setX((window.getScaledWidth() - getWidth()) / 2);

        float offsetY = 0;
        float offsetX = 5;
        for (Notification notification : list) {
            float anim = notification.anim.getOutput().floatValue();
            float width = font.getStringWidth(notification.text) + offsetX * 2;
            float startY = getY() + offsetY;
            float startX = getX() + (getWidth() - width) / 2;

            MathUtil.setAlpha(anim, () -> {
                blur.render(ShapeProperties.create(matrix, startX, startY, width, getHeight()).round(3)
                        .outlineColor(ColorUtil.getOutline()).color(ColorUtil.getRect(0.7F)).build());
                font.drawText(matrix, notification.text, (int) (startX + offsetX), startY + 6.5F);
            });
            offsetY += (getHeight() + 3) * anim;
        }
    }

    public void addList(String text, long removeTime) {
        addList(Text.empty().append(text), removeTime, null);
    }

    public void addList(Text text, long removeTime) {
        addList(text, removeTime, null);
    }

    public void addList(String text, long removeTime, SoundEvent sound) {
        addList(Text.empty().append(text), removeTime, sound);
    }

    public void addList(Text text, long removeTime, SoundEvent sound) {
        list.add(new Notification(text, new DecelerateAnimation().setMs(300).setValue(1), System.currentTimeMillis() + removeTime));
        if (list.size() > 12) list.removeFirst();
        list.sort(Comparator.comparingDouble(notif -> -notif.removeTime));
        if (sound != null) SoundManager.playSound(sound);
    }

    public record Notification(Text text, Animation anim, long removeTime) {}
}
