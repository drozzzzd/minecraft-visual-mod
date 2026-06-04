package torovvisual.adapter.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.util.math.MatrixStack;
import powder.api.event.EventSubscribe;
import powder.api.event.events.EventModuleToggle;
import torovvisual.api.system.animation.Animation;
import torovvisual.api.system.animation.Direction;
import torovvisual.api.system.animation.implement.DecelerateAnimation;
import torovvisual.api.system.font.FontRenderer;
import torovvisual.api.system.font.Fonts;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.render.ScissorManager;
import torovvisual.common.util.world.ServerUtil;
import torovvisual.core.Main;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ported from the Zenith "Dynamic Island" draggable to Powder's Torov Visual HUD
 * base ({@link HudElement}). The Windows media-session part (cover / track / progress)
 * is intentionally omitted because it relies on an external native library that is
 * not bundled here; everything else is kept: centered client name, clock, ping
 * bars, PVP timer, and module on/off notifications.
 */
public class DynamicIsland extends HudElement {

    private final Animation internetAnimation = new DecelerateAnimation().setMs(300).setValue(1);
    private final Animation mediaAnimation = new DecelerateAnimation().setMs(300).setValue(1);
    private final Animation pvpAnimation = new DecelerateAnimation().setMs(300).setValue(1);
    private final Animation barAnimation = new DecelerateAnimation().setMs(300).setValue(1);
    private final Animation moduleAnimation = new DecelerateAnimation().setMs(300).setValue(1);

    private final float[] currentBarHeights = new float[]{10f, 8f, 6f};

    private String currentModuleNotification = "";
    private String currentModuleNotificationClean = "";
    private long moduleNotificationTime = 0;
    private static final long MODULE_NOTIFICATION_DURATION = 2000;

    private static final Pattern PVP_TIMER_PATTERN = Pattern.compile("(\\d+)");

    public DynamicIsland() {
        super("DynamicIsland", 0, 4, 100, 18);
    }

    @Override
    public boolean visible() {
        return !fullNullCheck();
    }

    @EventSubscribe
    public void onModuleToggle(EventModuleToggle event) {
        if (event.getModule() == this) return;
        showModuleNotification(event.getModule().getName(), event.isEnabled());
    }

    @Override
    public void tick() {
        if (fullNullCheck()) return;
        updateAnimations();
    }

    private void updateAnimations() {
        int ping = currentPing();
        boolean isPvp = ServerUtil.isPvp();

        internetAnimation.setDirection(ping < 1000 ? Direction.FORWARDS : Direction.BACKWARDS);
        mediaAnimation.setDirection(Direction.BACKWARDS);
        pvpAnimation.setDirection(isPvp ? Direction.FORWARDS : Direction.BACKWARDS);
        barAnimation.setDirection(Direction.BACKWARDS);

        boolean showModuleNotification = !currentModuleNotification.isEmpty() &&
                System.currentTimeMillis() - moduleNotificationTime < MODULE_NOTIFICATION_DURATION;
        moduleAnimation.setDirection(showModuleNotification ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    public void showModuleNotification(String moduleName, boolean enabled) {
        currentModuleNotification = moduleName + " " + (enabled ? "§aEnabled" : "§cDisabled");
        currentModuleNotificationClean = moduleName + " " + (enabled ? "Enabled" : "Disabled");
        moduleNotificationTime = System.currentTimeMillis();
        moduleAnimation.reset();
    }

    @Override
    public void drawDraggable(DrawContext context) {
        if (fullNullCheck()) return;

        MatrixStack matrix = context.getMatrices();
        ScissorManager scissor = Main.getInstance().getScissorManager();

        String name = "Singularity";
        String pvp = "PVP";
        String pvpTimer = getPvpTimer();
        boolean isPvp = ServerUtil.isPvp();
        boolean showModuleNotification = !currentModuleNotification.isEmpty() &&
                System.currentTimeMillis() - moduleNotificationTime < MODULE_NOTIFICATION_DURATION;

        float padding = 2f;
        float round = 6f;

        FontRenderer font = Fonts.getSize(12, Fonts.Type.BOLD);
        float baseWidth;
        if (showModuleNotification) {
            baseWidth = 15 + font.getStringWidth(currentModuleNotificationClean) + padding * 2;
        } else if (isPvp) {
            baseWidth = 15 + font.getStringWidth(pvp) + padding * 3;
        } else {
            baseWidth = 15 + font.getStringWidth(name) + padding * 2;
        }

        float baseHeight = 15f;
        float width = baseWidth;
        float height = baseHeight;
        float x = window.getScaledWidth() / 2f - width / 2f;

        float bossBarOffset = 0f;
        if (mc.inGameHud != null && mc.inGameHud.getBossBarHud() != null) {
            int bossBarCount = mc.inGameHud.getBossBarHud().bossBars.size();
            if (bossBarCount > 0) {
                bossBarOffset = 19;
            }
        }

        float y = 4f + bossBarOffset;
        scissor.push(matrix.peek().getPositionMatrix(), x - 6, y - 6, width + 10, height + 10);
        blur.render(ShapeProperties.create(matrix, x - 1, y - 0.5f, width + 2, height + 1).round(round).softness(0.5f).color(ColorUtil.getColor(16, 16, 16, 180)).build());

        if (showModuleNotification && moduleAnimation.getOutput().floatValue() > 0.01f) {
            float alpha = moduleAnimation.getOutput().floatValue();
            Color dotColor = currentModuleNotification.contains("§a")
                    ? new Color(55, 255, 55, (int) (255 * alpha))
                    : new Color(255, 55, 55, (int) (255 * alpha));

            font.drawString(matrix, currentModuleNotificationClean, x + padding + 12, y - (padding / 2f) + (font.getStringHeight(currentModuleNotificationClean) / 2f), ColorUtil.getColor(255, 255, 255, (int) (255 * alpha)));
            rectangle.render(ShapeProperties.create(matrix, x + padding, y + padding, height - padding * 2, height - padding * 2).round(4f).color(ColorUtil.getColor(dotColor.getRed(), dotColor.getGreen(), dotColor.getBlue(), dotColor.getAlpha())).build());
        } else if (!isPvp) {
            float defaultAlpha = 1f - mediaAnimation.getOutput().floatValue();
            rectangle.render(ShapeProperties.create(matrix, x + padding, y + padding, baseHeight - padding * 2, baseHeight - padding * 2).round(4f).color(ColorUtil.multAlpha(ColorUtil.getClientColor(), defaultAlpha)).build());
            font.drawString(matrix, name, x + baseHeight, y - (padding / 2f) + (font.getStringHeight(name) / 2f), ColorUtil.multAlpha(ColorUtil.WHITE, defaultAlpha));
        } else if (pvpAnimation.getOutput().floatValue() > 0.01f) {
            float pvpAlpha = pvpAnimation.getOutput().floatValue();
            rectangle.render(ShapeProperties.create(matrix, x + padding, y + padding, baseHeight + 2f - padding * 2, baseHeight - padding * 2).round(4f).color(ColorUtil.multAlpha(ColorUtil.RED, pvpAlpha)).build());

            FontRenderer timerFont = Fonts.getSize(13, Fonts.Type.DEFAULT);
            timerFont.drawString(matrix, pvpTimer, x + baseHeight - timerFont.getStringWidth(pvpTimer) / 2 - padding * 3.5f, y - (padding / 2f) + (timerFont.getStringHeight(pvpTimer) / 1.5f), ColorUtil.multAlpha(ColorUtil.WHITE, pvpAlpha));
            font.drawString(matrix, pvp, x + baseHeight + padding, y - (padding / 2f) + (font.getStringHeight(pvp) / 2f), ColorUtil.multAlpha(ColorUtil.WHITE, pvpAlpha));
        }

        scissor.pop();

        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        FontRenderer timeFont = Fonts.getSize(13, Fonts.Type.DEFAULT);
        timeFont.drawString(matrix, currentTime, x - 1 - (padding * 3f) - timeFont.getStringWidth(currentTime), y - 0.5f - (padding / 2f) + (timeFont.getStringHeight(currentTime) / 2f), ColorUtil.WHITE);

        float internetAlpha = internetAnimation.getOutput().floatValue();
        float baseBarY = y + padding + (Fonts.getSize(7, Fonts.Type.DEFAULT).getStringHeight("P") / 2f) - 4 + 1;

        if (internetAlpha > 0.01f) {
            rectangle.render(ShapeProperties.create(matrix, x + width + (padding * 3f), baseBarY, 3.5F, currentBarHeights[0]).round(1f).color(ColorUtil.multAlpha(ColorUtil.WHITE, internetAlpha)).build());
            rectangle.render(ShapeProperties.create(matrix, x + width + (padding * 3f) + 4, baseBarY + 2, 3.5F, currentBarHeights[1]).round(1f).color(ColorUtil.multAlpha(ColorUtil.WHITE, internetAlpha)).build());
            rectangle.render(ShapeProperties.create(matrix, x + width + (padding * 3f) + 8, baseBarY + 4 - 0.5F, 3.5F, currentBarHeights[2] + 0.5F).round(1f).color(ColorUtil.multAlpha(ColorUtil.WHITE, internetAlpha)).build());
        }
    }

    private int currentPing() {
        if (mc.player != null && mc.getNetworkHandler() != null && mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()) != null) {
            return mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()).getLatency();
        }
        return 0;
    }

    private String getPvpTimer() {
        if (mc.inGameHud == null || mc.inGameHud.getBossBarHud() == null) {
            return "30";
        }
        for (ClientBossBar bossBar : mc.inGameHud.getBossBarHud().bossBars.values()) {
            String name = bossBar.getName().getString().toLowerCase();
            if (name.contains("pvp") || name.contains("пвп")) {
                Matcher matcher = PVP_TIMER_PATTERN.matcher(bossBar.getName().getString());
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return "30";
    }

    private boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }
}
