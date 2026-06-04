package torovvisual.adapter.hud;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import powder.api.event.EventSubscribe;
import powder.api.event.events.EventRender2D;
import powder.api.event.events.EventTickPlayer;
import powder.client.addon.Addon;
import powder.client.addon.Type;
import powder.client.gui.screens.draggableGui.comp.Draggable;
import torovvisual.api.system.animation.Animation;
import torovvisual.api.system.animation.Direction;
import torovvisual.api.system.animation.implement.DecelerateAnimation;
import torovvisual.common.QuickImports;

/**
 * Base for ported Torov Visual HUD elements. Each element is a Powder
 * {@link Addon} of {@link Type#HUD} so it shows up (and toggles) in the menu's
 * "Hud" category, and renders through Powder's {@link EventRender2D} hook using
 * the ported Torov Visual rendering (fonts / shapes via {@link QuickImports}).
 *
 * The original draggable editor is intentionally not ported: elements use their
 * built-in / self-computed positions.
 */
@Getter
@Setter
public abstract class HudElement extends Addon implements QuickImports {
    private int x, y, width, height;

    public final Animation scaleAnimation = new DecelerateAnimation().setValue(1).setMs(200);

    /**
     * Registered only for elements that opt into {@link #movable()}; lets the
     * element be repositioned by dragging it while the chat is open (see
     * {@code MixinChatScreen}). {@code null} for self-positioned elements.
     */
    private final Draggable draggable;

    public HudElement(String name, int x, int y, int width, int height) {
        super(name, Type.HUD);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if (movable()) {
            this.draggable = new Draggable(name, x, y, width, height);
            Draggable.draggables.add(this.draggable);
        } else {
            this.draggable = null;
        }
    }

    public boolean visible() {
        return true;
    }

    /**
     * Whether this element can be freely repositioned by dragging it in chat.
     * Self-positioned elements (e.g. the centered hotbar) leave this {@code false}.
     */
    public boolean movable() {
        return false;
    }

    public void tick() {
    }

    public abstract void drawDraggable(DrawContext context);

    public void startAnimation() {
        scaleAnimation.setDirection(Direction.FORWARDS);
    }

    public void stopAnimation() {
        scaleAnimation.setDirection(Direction.BACKWARDS);
    }

    @EventSubscribe
    public void onHudRender(EventRender2D event) {
        if (!isEnable() || !visible()) return;

        if (draggable != null) {
            setX((int) draggable.x);
            setY((int) draggable.y);
        }

        drawDraggable(event.getGraphics());

        if (draggable != null) {
            draggable.x = getX();
            draggable.y = getY();
            draggable.width = getWidth();
            draggable.height = getHeight();
        }
    }

    @EventSubscribe
    public void onPlayerTick(EventTickPlayer event) {
        if (isEnable()) tick();
    }
}
