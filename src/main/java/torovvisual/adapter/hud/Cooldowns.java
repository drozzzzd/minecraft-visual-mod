package torovvisual.adapter.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import torovvisual.api.system.font.FontRenderer;
import torovvisual.api.system.font.Fonts;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.common.util.color.ColorUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Item cooldown HUD. Ported from the Wonderful "Cooldowns" element to Powder's
 * Torov Visual HUD base ({@link HudElement}); it lists every inventory item that
 * is currently on cooldown with a shrinking progress bar. Movable by dragging it
 * while the chat is open (see {@code MixinChatScreen}).
 */
public class Cooldowns extends HudElement {

    public Cooldowns() {
        super("Cooldowns", 10, 60, 90, 30);
    }

    @Override
    public boolean movable() {
        return true;
    }

    @Override
    public boolean visible() {
        return mc.player != null;
    }

    @Override
    public void drawDraggable(DrawContext context) {
        if (mc.player == null) return;

        MatrixStack matrix = context.getMatrices();
        float x = getX();
        float y = getY();
        float padding = 5f;

        FontRenderer title = Fonts.getSize(14, Fonts.Type.BOLD);
        FontRenderer font = Fonts.getSize(12, Fonts.Type.DEFAULT);

        // Collect items currently on cooldown (de-duplicated by item type).
        PlayerInventory inventory = mc.player.getInventory();
        float tickDelta = tickCounter.getTickDelta(false);
        Set<Item> seen = new LinkedHashSet<>();
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            if (!mc.player.getItemCooldownManager().isCoolingDown(stack)) continue;
            if (!seen.add(stack.getItem())) continue;
            float progress = mc.player.getItemCooldownManager().getCooldownProgress(stack, tickDelta);
            entries.add(new Entry(displayName(stack), progress));
        }

        float barWidth = 30f;
        float rowHeight = 13f;
        float headerHeight = title.getStringHeight("Cooldowns") + padding * 2;

        float contentWidth = title.getStringWidth("Cooldowns");
        for (Entry e : entries) {
            contentWidth = Math.max(contentWidth, font.getStringWidth(e.name) + barWidth + padding * 2);
        }
        float width = Math.max(contentWidth + padding * 2, 80f);
        float height = headerHeight + 3f + entries.size() * rowHeight + padding;

        setWidth((int) width);
        setHeight((int) height);

        blur.render(ShapeProperties.create(matrix, x, y, width, height).round(4).softness(1).thickness(2)
                .outlineColor(ColorUtil.getOutline()).color(ColorUtil.getRect(0.7F)).build());

        title.drawCenteredString(matrix, "Cooldowns", x + width / 2f, y + padding, ColorUtil.getText());
        rectangle.render(ShapeProperties.create(matrix, x + 4, y + headerHeight - 1, width - 8, 1f)
                .round(0.5f).color(ColorUtil.getOutline(0.6f, 1)).build());

        float rowY = y + headerHeight + 3f;
        for (Entry e : entries) {
            font.drawString(matrix, e.name, x + padding, rowY, ColorUtil.getText());

            float barX = x + width - padding - barWidth;
            float barY = rowY + 5f;
            float p = Math.max(0f, Math.min(1f, e.progress));
            rectangle.render(ShapeProperties.create(matrix, barX, barY, barWidth, 2.5f).round(1)
                    .color(ColorUtil.getColor(12, 12, 12)).build());
            rectangle.render(ShapeProperties.create(matrix, barX, barY, barWidth * p, 2.5f).round(1)
                    .color(ColorUtil.getClientColor()).build());

            rowY += rowHeight;
        }
    }

    private static String displayName(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.ENCHANTED_GOLDEN_APPLE) return "Чарка";
        if (item == Items.ENDER_EYE) return "Дезорент";
        if (item == Items.NETHERITE_SCRAP) return "Трапка";
        if (item == Items.SUGAR) return "Явная пыль";
        if (item == Items.ENDER_PEARL) return "Эндер-перл";
        if (item == Items.GOLDEN_APPLE) return "Гэпл";
        if (item == Items.DRIED_KELP) return "Пласт";
        return stack.getName().getString();
    }

    private record Entry(String name, float progress) {
    }
}
