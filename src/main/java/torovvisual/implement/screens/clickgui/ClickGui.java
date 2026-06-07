package torovvisual.implement.screens.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import torovvisual.api.feature.module.Module;
import torovvisual.api.feature.module.ModuleCategory;
import torovvisual.api.feature.module.setting.Setting;
import torovvisual.api.feature.module.setting.implement.BooleanSetting;
import torovvisual.api.feature.module.setting.implement.ValueSetting;
import torovvisual.api.system.animation.Animation;
import torovvisual.api.system.animation.implement.DecelerateAnimation;
import torovvisual.api.system.font.RichFonts;
import torovvisual.api.system.sound.SoundManager;
import torovvisual.common.QuickImports;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.math.MathUtil;
import torovvisual.common.util.other.StringUtil;
import torovvisual.core.Main;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static torovvisual.api.system.animation.Direction.BACKWARDS;
import static torovvisual.api.system.animation.Direction.FORWARDS;

/**
 * Rich-Modern ClickGUI reproduced on the Torov Visual (MC 1.21.4) framework: the
 * dark grayscale panel, left category column with MSDF icons, header bar, 22px
 * module rows (state ball, bind box, gear icon) and the settings panel — drawn
 * with {@link R2D} primitives + {@link RichFonts} (the source's own MSDF fonts).
 *
 * <p>Bound to the existing module model ({@link Main}'s repository of Powder addons
 * wrapped as {@link Module}); only RENDER / MOVEMENT / HUD are shown.
 */
public class ClickGui extends Screen implements QuickImports {

    public static final ClickGui INSTANCE = new ClickGui();

    private static final float BG_W = 400, BG_H = 250;

    private static final ModuleCategory[] CATEGORIES = {
            ModuleCategory.RENDER, ModuleCategory.MOVEMENT, ModuleCategory.HUD
    };
    private static final Map<ModuleCategory, String> CAT_NAME = new HashMap<>();
    private static final Map<ModuleCategory, String> CAT_ICON = new HashMap<>();
    static {
        CAT_NAME.put(ModuleCategory.RENDER, "Render");
        CAT_NAME.put(ModuleCategory.MOVEMENT, "Movement");
        CAT_NAME.put(ModuleCategory.HUD, "Hud");
        CAT_ICON.put(ModuleCategory.RENDER, "c");
        CAT_ICON.put(ModuleCategory.MOVEMENT, "b");
        CAT_ICON.put(ModuleCategory.HUD, "d");
    }

    private final Animation animation = new DecelerateAnimation().setMs(250).setValue(1);

    private ModuleCategory selectedCategory = ModuleCategory.RENDER;
    private Module selectedModule, bindingModule;
    private boolean settingsBinding;

    private float moduleScroll, settingScroll;

    private float offsetX, offsetY;
    private boolean dragging;
    private float dragMouseX, dragMouseY, dragStartX, dragStartY;
    private ValueSetting draggingSetting;

    private final Map<ModuleCategory, Float> catAnim = new HashMap<>();
    private final Map<Module, Float> hoverAnim = new IdentityHashMap<>();
    private final Map<Module, Float> stateAnim = new IdentityHashMap<>();
    private long lastTime = System.currentTimeMillis();

    private final List<SettingRow> settingRows = new ArrayList<>();
    private float enX, enY, enW, enH, bindX, bindY, bindW, bindH;

    // search
    private boolean searchActive;
    private String searchText = "";
    private final List<Row> searchRows = new ArrayList<>();
    private float searchBoxX, searchBoxY, searchBoxW, searchBoxH;

    // theme picker swatches
    private static final int[] THEME_COLORS = {0xFF6C9AFD, 0xFFFF5555, 0xFF55FF77, 0xFFFFFFFF, 0xFFB36CFF, 0xFFFFB000};
    private final float[] swatchX = new float[THEME_COLORS.length];
    private float swatchY;

    // tooltip (drawn last, above everything)
    private String tooltip;
    private float tooltipX, tooltipY;

    private static int theme() {
        return ColorUtil.getClientColor();
    }

    public ClickGui() {
        super(Text.of("ClickGui"));
    }

    public void openGui() {
        animation.setDirection(FORWARDS);
        selectedCategory = ModuleCategory.RENDER;
        moduleScroll = settingScroll = 0;
        settingsBinding = false;
        bindingModule = null;
        mc.setScreen(this);
        SoundManager.playSound(SoundManager.OPEN_GUI);
    }

    private float scale() {
        return animation.getOutput().floatValue();
    }

    private float dt() {
        long now = System.currentTimeMillis();
        float d = Math.min((now - lastTime) / 1000f, 0.1f);
        lastTime = now;
        return d;
    }

    private static float approach(float current, float target, float dt, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) return target;
        return current + diff * Math.min(1f, speed * dt);
    }

    @Override
    public void tick() {
        close();
        super.tick();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // own dim overlay; keep the world sharp
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        R2D.begin(matrix);
        RichFonts.begin(matrix);

        float frame = dt();

        float px = window.getScaledWidth() / 2f - BG_W / 2f + offsetX;
        float py = window.getScaledHeight() / 2f - BG_H / 2f + offsetY;

        R2D.rect(0, 0, window.getScaledWidth(), window.getScaledHeight(),
                new Color(0, 0, 0, (int) (130 * scale())).getRGB());

        MathUtil.scale(matrix, px + BG_W / 2f, py + BG_H / 2f, scale(),
                () -> drawAll(px, py, mouseX, mouseY, frame));

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawAll(float bgX, float bgY, int mouseX, int mouseY, float frame) {
        tooltip = null;
        drawBackground(bgX, bgY);
        drawCategories(bgX, bgY, mouseX, mouseY, frame);
        drawThemeSwatches(bgX, bgY, mouseX, mouseY);
        drawHeader(bgX, bgY, mouseX, mouseY);
        if (searchActive && !searchText.isEmpty()) {
            drawSearchResults(bgX, bgY, mouseX, mouseY);
        } else {
            drawModuleList(bgX, bgY, mouseX, mouseY, frame);
        }
        drawSettings(bgX, bgY, mouseX, mouseY);
        drawTooltip();
    }

    // ── background ───────────────────────────────────────────────────────────

    private void drawBackground(float bgX, float bgY) {
        int[] gradient = {
                new Color(26, 26, 26).getRGB(),
                new Color(8, 8, 9).getRGB(),
                new Color(8, 8, 9).getRGB(),
                new Color(26, 26, 22).getRGB()
        };
        R2D.gradientRect(bgX, bgY, BG_W, BG_H, gradient, 10);
        R2D.outline(bgX, bgY, BG_W, BG_H, 0.5f, new Color(48, 48, 52).getRGB(), 10);

        // category column
        R2D.rect(bgX + 7.5f, bgY + 7.5f, 80, BG_H - 15, new Color(128, 128, 128, 22).getRGB(), 9);
        R2D.outline(bgX + 7.5f, bgY + 7.5f, 80, BG_H - 15, 0.5f, new Color(48, 48, 52).getRGB(), 9);

        // brand (with soft glow)
        RichFonts.BOLD.drawGlow("Torov", bgX + 15f, bgY + 14f, 8, new Color(235, 235, 240).getRGB(), new Color(255, 255, 255, 45).getRGB());
        RichFonts.BOLD.drawGlow("Visual", bgX + 15f, bgY + 23f, 8, theme(), ColorUtil.multAlpha(theme(), 0.30F));

        // bottom X/Y/Z + Soon box (Rich detail)
        R2D.outline(bgX + 12.5f, bgY + 220.5f, 70, 17, 0.5f, new Color(55, 55, 55).getRGB(), 5);
        RichFonts.GUI_ICONS.draw("X", bgX + 21.15f, bgY + 217.5f, 19, new Color(58, 58, 58).getRGB());
        RichFonts.GUI_ICONS.draw("Y", bgX + 40f, bgY + 217f, 20, new Color(58, 58, 58).getRGB());
        RichFonts.GUI_ICONS.draw("Z", bgX + 60f, bgY + 217f, 20, new Color(58, 58, 58).getRGB());
        String soon = "Soon...";
        float sw = RichFonts.BOLD.getWidth(soon, 6);
        RichFonts.BOLD.draw(soon, bgX + 12.5f + (70 - sw) / 2f, bgY + 226f, 6, new Color(150, 150, 150, 200).getRGB());
    }

    // ── categories ───────────────────────────────────────────────────────────

    private void drawCategories(float bgX, float bgY, int mouseX, int mouseY, float frame) {
        // section header "Основные"
        renderSectionHeader(bgX, bgY + 52f, "Основные");

        for (int i = 0; i < CATEGORIES.length; i++) {
            ModuleCategory cat = CATEGORIES[i];
            float target = cat == selectedCategory ? 1f : 0f;
            float anim = approach(catAnim.getOrDefault(cat, 0f), target, frame, 8f);
            catAnim.put(cat, anim);

            float textY = bgY + 65f + i * 15f;
            renderCategoryItem(bgX, textY, CAT_NAME.get(cat), CAT_ICON.get(cat), anim);
        }
    }

    private void renderSectionHeader(float bgX, float sectionY, String title) {
        float lineWidth = 18f, totalWidth = 65f;
        float textWidth = RichFonts.BOLD.getWidth(title, 5);
        float textX = bgX + 15f + (totalWidth - textWidth) / 2f;
        float lineY = sectionY + 3f;
        R2D.rect(bgX + 15f, lineY, lineWidth, 0.5f, new Color(255, 255, 255, 40).getRGB());
        R2D.rect(bgX + 15f + totalWidth - lineWidth, lineY, lineWidth, 0.5f, new Color(255, 255, 255, 40).getRGB());
        RichFonts.BOLD.draw(title, textX, sectionY, 5, new Color(150, 150, 150, 100).getRGB());
    }

    private void renderCategoryItem(float bgX, float textY, String name, String icon, float anim) {
        float offset = anim * 5f;
        int colorValue = (int) (128 + 127 * anim);
        int alpha = (int) (128 + 127 * anim);
        int color = new Color(colorValue, colorValue, colorValue, alpha).getRGB();

        float iconX = bgX + 17f + offset;
        float iconWidth = RichFonts.CATEGORY_ICONS.getWidth(icon, 6);
        float textX = iconX + iconWidth + 4f;

        RichFonts.CATEGORY_ICONS.draw(icon, iconX, textY + 0.5f, 6, color);
        if (anim > 0.4f) {
            RichFonts.BOLD.drawGlow(name, textX, textY, 6, color, new Color(255, 255, 255, (int) (anim * 45)).getRGB());
        } else {
            RichFonts.BOLD.draw(name, textX, textY, 6, color);
        }

        if (anim > 0.01f) {
            float textWidth = RichFonts.BOLD.getWidth(name, 6);
            float lineWidth = (iconWidth + 4f + textWidth) * anim;
            R2D.rect(iconX, textY + 9f, lineWidth, 0.5f, new Color(255, 255, 255, (int) (anim * 60)).getRGB());
            R2D.rect(bgX + 12f, textY + 2.5f, 3, 3, new Color(255, 255, 255, (int) (anim * 200)).getRGB(), 1.5f);
        }
    }

    // ── header ───────────────────────────────────────────────────────────────

    private void drawHeader(float bgX, float bgY, int mouseX, int mouseY) {
        R2D.rect(bgX + 92f, bgY + 7.5f, BG_W - 100f, 25, new Color(128, 128, 128, 22).getRGB(), 8);
        R2D.outline(bgX + 92f, bgY + 7.5f, BG_W - 100f, 25, 0.5f, new Color(48, 48, 52).getRGB(), 8);

        String headerLabel = searchActive && !searchText.isEmpty() ? "Results" : CAT_NAME.get(selectedCategory);
        RichFonts.BOLD.draw(headerLabel, bgX + 100f, bgY + 16f, 7, new Color(170, 170, 170).getRGB());

        searchBoxX = bgX + 315f;
        searchBoxY = bgY + 12.5f;
        searchBoxW = 70f;
        searchBoxH = 15f;
        R2D.rect(searchBoxX, searchBoxY, searchBoxW, searchBoxH, new Color(40, 40, 45, 25).getRGB(), 4);
        R2D.outline(searchBoxX, searchBoxY, searchBoxW, searchBoxH, 0.5f,
                searchActive ? new Color(170, 170, 175).getRGB() : new Color(55, 55, 55).getRGB(), 4);

        if (searchText.isEmpty() && !searchActive) {
            RichFonts.BOLD.draw("Search...", searchBoxX + 5, searchBoxY + 5f, 5, new Color(120, 120, 120).getRGB());
        } else {
            R2D.scissorPush(searchBoxX + 3, searchBoxY, searchBoxW - 18, searchBoxH);
            RichFonts.BOLD.draw(searchText, searchBoxX + 5, searchBoxY + 5f, 5, new Color(210, 210, 215).getRGB());
            if (searchActive && (System.currentTimeMillis() / 500) % 2 == 0) {
                float cx = searchBoxX + 5 + RichFonts.BOLD.getWidth(searchText, 5);
                R2D.rect(cx, searchBoxY + 3.5f, 0.6f, searchBoxH - 7, new Color(200, 200, 205).getRGB());
            }
            R2D.scissorPop();
        }
        R2D.rect(searchBoxX + 53, searchBoxY + 3.5f, 1, searchBoxH - 7, new Color(128, 128, 128, 60).getRGB());
        RichFonts.ICONS.draw("U", searchBoxX + 55, searchBoxY + 1.5f, 12,
                searchActive ? theme() : new Color(128, 128, 128).getRGB());
    }

    // ── theme swatches + search results + tooltip ────────────────────────────

    private void drawThemeSwatches(float bgX, float bgY, int mouseX, int mouseY) {
        float startX = bgX + 14f;
        swatchY = bgY + BG_H - 36f;
        RichFonts.BOLD.draw("Theme", startX, swatchY - 9, 5, new Color(150, 150, 150, 150).getRGB());
        float gap = 11.5f, sz = 8.5f;
        for (int i = 0; i < THEME_COLORS.length; i++) {
            float sx = startX + i * gap;
            swatchX[i] = sx;
            boolean selected = ColorUtil.getClientColor() == THEME_COLORS[i];
            boolean hover = MathUtil.isHovered(mouseX, mouseY, sx, swatchY, sz, sz);
            R2D.rect(sx, swatchY, sz, sz, THEME_COLORS[i], 2.5f);
            if (selected || hover) {
                R2D.outline(sx, swatchY, sz, sz, selected ? 1f : 0.6f,
                        selected ? new Color(255, 255, 255).getRGB() : new Color(255, 255, 255, 130).getRGB(), 2.5f);
            }
        }
    }

    private void drawSearchResults(float bgX, float bgY, int mouseX, int mouseY) {
        searchRows.clear();
        float x = bgX + 92f, y = bgY + 38f, w = BG_W - 100f, h = BG_H - 46f;
        R2D.rect(x, y, w, h, new Color(64, 64, 64, 15).getRGB(), 6);
        R2D.outline(x, y, w, h, 0.5f, new Color(55, 55, 55, 215).getRGB(), 6);

        String q = searchText.toLowerCase();
        List<Module> results = new ArrayList<>();
        for (Module m : Main.getInstance().getModuleRepository().modules()) {
            ModuleCategory c = m.getCategory();
            boolean shown = c == ModuleCategory.RENDER || c == ModuleCategory.MOVEMENT || c == ModuleCategory.HUD;
            if (shown && m.getVisibleName().toLowerCase().contains(q)) results.add(m);
        }

        float rowH = 19, gap = 3;
        float content = results.size() * (rowH + gap);
        moduleScroll = MathHelper.clamp(moduleScroll, Math.min(0, h - content - 6), 0);
        R2D.scissorPush(x + 3, y + 1.5f, w - 6, h - 3);
        float ry = y + 5 + moduleScroll;
        for (Module m : results) {
            if (ry + rowH >= y && ry <= y + h) {
                boolean hover = MathUtil.isHovered(mouseX, mouseY, x + 3, ry, w - 6, rowH);
                boolean on = m.isState();
                R2D.rect(x + 3, ry, w - 6, rowH, new Color(64, 64, 64, hover ? 45 : 25).getRGB(), 5);
                RichFonts.BOLD.draw(m.getVisibleName(), x + 9, ry + 6, 6, on ? theme() : new Color(196, 198, 208).getRGB());
                RichFonts.BOLD.draw(CAT_NAME.get(m.getCategory()), x + w - 9 - RichFonts.BOLD.getWidth(CAT_NAME.get(m.getCategory()), 5),
                        ry + 6.5f, 5, new Color(120, 120, 128).getRGB());
                if (hover) setTooltip(m.getVisibleName(), mouseX, mouseY);
            }
            searchRows.add(new Row(m, x + 3, ry, w - 6, rowH));
            ry += rowH + gap;
        }
        R2D.scissorPop();
    }

    private void setTooltip(String text, int mouseX, int mouseY) {
        if (text == null || text.isEmpty()) return;
        tooltip = text;
        tooltipX = mouseX;
        tooltipY = mouseY;
    }

    private void drawTooltip() {
        if (tooltip == null) return;
        float tw = RichFonts.BOLD.getWidth(tooltip, 6);
        float x = tooltipX + 8, y = tooltipY + 8;
        R2D.rect(x - 3, y - 3, tw + 6, 13, new Color(20, 20, 23, 235).getRGB(), 3);
        R2D.outline(x - 3, y - 3, tw + 6, 13, 0.5f, new Color(70, 70, 75).getRGB(), 3);
        RichFonts.BOLD.draw(tooltip, x, y, 6, new Color(225, 225, 230).getRGB());
    }

    // ── module list ──────────────────────────────────────────────────────────

    private List<Module> modulesFor(ModuleCategory category) {
        List<Module> list = new ArrayList<>();
        for (Module module : Main.getInstance().getModuleRepository().modules()) {
            if (module.getCategory() == category) list.add(module);
        }
        return list;
    }

    private void drawModuleList(float bgX, float bgY, int mouseX, int mouseY, float frame) {
        float x = bgX + 92f, y = bgY + 38f, w = 120f, h = BG_H - 46f;

        R2D.rect(x, y, w, h, new Color(64, 64, 64, 15).getRGB(), 6);
        R2D.outline(x, y, w, h, 0.5f, new Color(55, 55, 55, 215).getRGB(), 6);

        List<Module> modules = modulesFor(selectedCategory);
        float itemH = 22f;
        float content = modules.size() * (itemH + 2);
        moduleScroll = MathHelper.clamp(moduleScroll, Math.min(0, h - content - 6), 0);

        R2D.scissorPush(x + 3, y + 1.5f, w - 6, h - 3);

        float startY = y + 3 + 2 + moduleScroll;
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            float modY = startY + i * (itemH + 2);
            if (modY + itemH < y || modY > y + h) continue;

            boolean hover = MathUtil.isHovered(mouseX, mouseY, x + 3, modY, w - 6, itemH);
            float hAnim = approach(hoverAnim.getOrDefault(module, 0f), hover ? 1f : 0f, frame, 12f);
            hoverAnim.put(module, hAnim);
            float sAnim = approach(stateAnim.getOrDefault(module, module.isState() ? 1f : 0f), module.isState() ? 1f : 0f, frame, 12f);
            stateAnim.put(module, sAnim);

            boolean selected = module == selectedModule;
            float itemX = x + 3, itemW = w - 6;

            int bgColor;
            if (selected) {
                bgColor = new Color(71, 71, 71, (int) (55 + hAnim * 10)).getRGB();
            } else {
                int gray = (int) (64 + 36 * hAnim);
                bgColor = new Color(gray, gray, gray, (int) (25 + 20 * hAnim)).getRGB();
            }
            R2D.rect(itemX, modY, itemW, itemH, bgColor, 5);
            if (selected) {
                R2D.outline(itemX, modY, itemW, itemH, 0.5f, new Color(95, 95, 100, 110).getRGB(), 5);
            } else if (hAnim > 0.01f) {
                R2D.outline(itemX, modY, itemW, itemH, 0.5f, new Color(120, 120, 120, (int) (60 * hAnim)).getRGB(), 5);
            }

            float stateOffset = sAnim * 6f;
            if (sAnim > 0.01f) {
                R2D.rect(itemX + 4, modY + (itemH - 3) / 2f + 1, 3, 3, new Color(255, 255, 255, (int) (sAnim * 220)).getRGB(), 1.5f);
            }

            int brightness = (int) (128 + 127 * sAnim);
            if (hAnim > 0.01f && sAnim < 0.99f) brightness = Math.min(255, (int) (brightness + 40 * hAnim));
            int textAlpha = Math.min(255, (int) (180 + 75 * sAnim));
            RichFonts.BOLD.draw(module.getVisibleName(), itemX + 5 + stateOffset, modY + (itemH - 6f) / 2f,
                    6, new Color(brightness, brightness, brightness, textAlpha).getRGB());

            // bind box
            int key = module.getKey();
            boolean isBinding = module == bindingModule;
            if (isBinding || (key != GLFW.GLFW_KEY_UNKNOWN && key != -1)) {
                String bindText = isBinding ? "..." : StringUtil.getBindName(key);
                float tw = RichFonts.BOLD.getWidth(bindText, 5);
                float nameW = RichFonts.BOLD.getWidth(module.getVisibleName(), 6);
                float boxW = Math.max(18f, tw + 12f);
                float boxX = itemX + 5 + stateOffset + nameW + 3;
                float boxY = modY + (itemH - 9f) / 2f + 0.5f;
                R2D.rect(boxX, boxY, boxW - 6, 9, new Color(50, 50, 55, 60).getRGB(), 3f);
                R2D.outline(boxX, boxY, boxW - 6, 9, 0.5f, new Color(80, 80, 85, 90).getRGB(), 3f);
                RichFonts.BOLD.draw(bindText, boxX + (boxW - 6 - tw) / 2f, boxY + 2f, 5, new Color(150, 150, 150).getRGB());
            }

            // settings/gear + dots icon
            boolean hasSettings = !module.settings().isEmpty();
            float iconX = itemX + itemW - 14;
            float iconY = modY + (itemH - 8f) / 2f;
            if (hasSettings) {
                if (selected) {
                    RichFonts.GUI_ICONS.draw("B", iconX, iconY + 1, 8, new Color(200, 200, 200).getRGB());
                } else {
                    RichFonts.BOLD.draw("...", iconX + 1f, iconY - 1f, 7, new Color(150, 150, 150, (int) (120 * (0.4f + hAnim))).getRGB());
                }
            }
        }

        R2D.scissorPop();
    }

    // ── settings ─────────────────────────────────────────────────────────────

    private void drawSettings(float bgX, float bgY, int mouseX, int mouseY) {
        settingRows.clear();
        enW = enH = bindW = bindH = 0;

        float x = bgX + 218f, y = bgY + 38f, w = 172f, h = BG_H - 46f;
        R2D.rect(x, y, w, h, new Color(64, 64, 64, 15).getRGB(), 6);
        R2D.outline(x, y, w, h, 0.5f, new Color(55, 55, 55, 215).getRGB(), 6);

        if (selectedModule == null) {
            RichFonts.BOLD.draw("Select a feature", x + 8, y + 8, 6, new Color(110, 110, 115).getRGB());
            return;
        }

        R2D.scissorPush(x + 3, y + 1.5f, w - 6, h - 3);
        float ry = y + 4 - settingScroll;

        // header
        R2D.rect(x + 4, ry, w - 8, 26, new Color(80, 80, 85, 30).getRGB(), 5);
        RichFonts.BOLD.draw(selectedModule.getVisibleName(), x + 9, ry + 5, 7, new Color(235, 235, 240).getRGB());

        enX = x + w - 28; enY = ry + 5; enW = 20; enH = 10;
        drawToggle(enX, enY, enW, enH, selectedModule.isState());

        String bindName = StringUtil.getBindName(selectedModule.getKey());
        String bindLabel = settingsBinding ? "..." : bindName;
        bindW = RichFonts.BOLD.getWidth(bindLabel, 5) + 8;
        bindX = x + 9; bindY = ry + 16; bindH = 9;
        R2D.rect(bindX, bindY, bindW, bindH, new Color(50, 50, 55, 60).getRGB(), 2);
        R2D.outline(bindX, bindY, bindW, bindH, 0.5f, new Color(80, 80, 85, 90).getRGB(), 2);
        RichFonts.BOLD.draw(bindLabel, bindX + 4, bindY + 2f, 5, new Color(150, 150, 150).getRGB());

        ry += 32;

        for (Setting setting : selectedModule.settings()) {
            if (!setting.isVisible()) continue;
            float rowH;

            if (setting instanceof BooleanSetting bool) {
                rowH = 18;
                RichFonts.BOLD.draw(setting.getName(), x + 9, ry + 6, 6, new Color(190, 190, 195).getRGB());
                drawToggle(x + w - 30, ry + (rowH - 10) / 2f, 20, 10, bool.isValue());
            } else if (setting instanceof ValueSetting value && value.hasOptions()) {
                rowH = 18;
                RichFonts.BOLD.draw(setting.getName(), x + 9, ry + 6, 6, new Color(190, 190, 195).getRGB());
                String disp = optionLabel(value);
                float rightX = x + w - 14;
                float vw = RichFonts.BOLD.getWidth(disp, 6);
                float vx = rightX - 6 - vw, leftX = vx - 9;
                boolean lh = MathUtil.isHovered(mouseX, mouseY, leftX - 2, ry + 2, 9, 12);
                boolean rh = MathUtil.isHovered(mouseX, mouseY, rightX - 2, ry + 2, 9, 12);
                RichFonts.BOLD.draw("<", leftX, ry + 5, 7, lh ? new Color(235, 235, 235).getRGB() : new Color(150, 150, 150).getRGB());
                RichFonts.BOLD.draw(disp, vx, ry + 6, 6, new Color(210, 210, 215).getRGB());
                RichFonts.BOLD.draw(">", rightX, ry + 5, 7, rh ? new Color(235, 235, 235).getRGB() : new Color(150, 150, 150).getRGB());
            } else if (setting instanceof ValueSetting value) {
                rowH = 24;
                RichFonts.BOLD.draw(setting.getName(), x + 9, ry + 4, 6, new Color(190, 190, 195).getRGB());
                String num = value.isInteger() ? String.valueOf(value.getInt()) : String.format("%.1f", value.getValue());
                float vw = RichFonts.BOLD.getWidth(num, 6);
                RichFonts.BOLD.draw(num, x + w - 12 - vw, ry + 4, 6, new Color(210, 210, 215).getRGB());
                float trackX = x + 9, trackY = ry + rowH - 9, trackW = w - 22;
                float frac = range(value) == 0 ? 0 : MathHelper.clamp((value.getValue() - value.getMin()) / range(value), 0, 1);
                R2D.rect(trackX, trackY, trackW, 3, new Color(40, 40, 44).getRGB(), 1.5f);
                R2D.rect(trackX, trackY, trackW * frac, 3, new Color(210, 210, 215).getRGB(), 1.5f);
                R2D.rect(trackX + trackW * frac - 2.5f, trackY - 1.5f, 5, 6, new Color(255, 255, 255).getRGB(), 2.5f);
            } else {
                continue;
            }
            if (MathUtil.isHovered(mouseX, mouseY, x, ry, w, rowH)) {
                String desc = setting.getDescription();
                setTooltip(desc != null && !desc.isEmpty() ? desc : setting.getName(), mouseX, mouseY);
            }
            settingRows.add(new SettingRow(setting, x, ry, w, rowH));
            ry += rowH + 2;
        }

        R2D.scissorPop();

        float total = 36;
        for (SettingRow row : settingRows) total += row.h + 2;
        settingScroll = MathHelper.clamp(settingScroll, 0, Math.max(0, total - h));
    }

    private void drawToggle(float x, float y, float w, float h, boolean on) {
        int bg = on ? new Color(220, 220, 225).getRGB() : new Color(45, 45, 50).getRGB();
        int outline = on ? new Color(220, 220, 225).getRGB() : new Color(70, 70, 75).getRGB();
        R2D.rect(x, y, w, h, bg, h / 2);
        R2D.outline(x, y, w, h, 0.5f, outline, h / 2);
        float knob = h - 4;
        float kx = on ? x + w - knob - 2 : x + 2;
        R2D.rect(kx, y + 2, knob, knob, on ? new Color(30, 30, 33).getRGB() : new Color(140, 140, 145).getRGB(), knob / 2);
    }

    private static float range(ValueSetting v) {
        return v.getMax() - v.getMin();
    }

    private static String optionLabel(ValueSetting v) {
        String[] options = v.getOptions();
        int index = (int) v.getValue() - (int) v.getMin();
        if (options != null && index >= 0 && index < options.length) return options[index];
        return String.valueOf((int) v.getValue());
    }

    // ── input ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float bgX = window.getScaledWidth() / 2f - BG_W / 2f + offsetX;
        float bgY = window.getScaledHeight() / 2f - BG_H / 2f + offsetY;

        if (button == 2 && MathUtil.isHovered(mouseX, mouseY, bgX, bgY, BG_W, BG_H)) {
            dragging = true;
            dragMouseX = (float) mouseX; dragMouseY = (float) mouseY;
            dragStartX = offsetX; dragStartY = offsetY;
            return true;
        }

        // search box focus
        if (button == 0 && searchBoxW > 0 && MathUtil.isHovered(mouseX, mouseY, searchBoxX, searchBoxY, searchBoxW, searchBoxH)) {
            searchActive = true;
            return true;
        }

        // theme swatches
        if (button == 0 && swatchY > 0) {
            for (int i = 0; i < THEME_COLORS.length; i++) {
                if (MathUtil.isHovered(mouseX, mouseY, swatchX[i], swatchY, 8.5f, 8.5f)) {
                    ColorUtil.setClientColor(THEME_COLORS[i]);
                    return true;
                }
            }
        }

        // search results
        if (searchActive && !searchText.isEmpty()) {
            for (Row row : searchRows) {
                if (row.contains(mouseX, mouseY)) {
                    selectedModule = row.module();
                    settingScroll = 0;
                    if (button == 0) row.module().switchState();
                    return true;
                }
            }
        }

        // categories
        for (int i = 0; i < CATEGORIES.length; i++) {
            float catY = bgY + 65f + i * 15f;
            if (MathUtil.isHovered(mouseX, mouseY, bgX + 10f, catY, 76f, 13f)) {
                if (selectedCategory != CATEGORIES[i]) {
                    selectedCategory = CATEGORIES[i];
                    moduleScroll = 0;
                }
                searchActive = false;
                searchText = "";
                return true;
            }
        }

        // module rows
        float mlX = bgX + 92f, mlY = bgY + 38f, mlW = 120f, mlH = BG_H - 46f;
        if (MathUtil.isHovered(mouseX, mouseY, mlX, mlY, mlW, mlH)) {
            List<Module> modules = modulesFor(selectedCategory);
            float startY = mlY + 5 + moduleScroll;
            for (int i = 0; i < modules.size(); i++) {
                float modY = startY + i * 24f;
                if (MathUtil.isHovered(mouseX, mouseY, mlX + 3, modY, mlW - 6, 22f)) {
                    Module module = modules.get(i);
                    selectedModule = module;
                    settingScroll = 0;
                    settingsBinding = false;
                    if (button == 0) module.switchState();
                    else if (button == 2) bindingModule = module;
                    return true;
                }
            }
        }

        // settings
        if (selectedModule != null) {
            if (enW > 0 && MathUtil.isHovered(mouseX, mouseY, enX, enY, enW, enH) && button == 0) {
                selectedModule.switchState();
                return true;
            }
            if (bindW > 0 && MathUtil.isHovered(mouseX, mouseY, bindX, bindY, bindW, bindH) && button == 0) {
                settingsBinding = !settingsBinding;
                return true;
            }
            for (SettingRow row : settingRows) {
                if (handleSettingClick(row, mouseX, mouseY, button)) return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleSettingClick(SettingRow row, double mouseX, double mouseY, int button) {
        Setting setting = row.setting;
        if (button != 0) return false;
        if (setting instanceof BooleanSetting bool) {
            if (MathUtil.isHovered(mouseX, mouseY, row.x, row.y, row.w, row.h)) {
                bool.setValue(!bool.isValue());
                return true;
            }
        } else if (setting instanceof ValueSetting value && value.hasOptions()) {
            float rightX = row.x + row.w - 14;
            float vw = RichFonts.BOLD.getWidth(optionLabel(value), 6);
            float vx = rightX - 6 - vw, leftX = vx - 9;
            if (MathUtil.isHovered(mouseX, mouseY, leftX - 2, row.y + 2, 9, 12)) { step(value, -1); return true; }
            if (MathUtil.isHovered(mouseX, mouseY, rightX - 2, row.y + 2, 9, 12)) { step(value, 1); return true; }
        } else if (setting instanceof ValueSetting value) {
            if (MathUtil.isHovered(mouseX, mouseY, row.x, row.y, row.w, row.h)) {
                draggingSetting = value;
                applySlider(value, mouseX);
                return true;
            }
        }
        return false;
    }

    private void applySlider(ValueSetting value, double mouseX) {
        SettingRow row = settingRowFor(value);
        if (row == null) return;
        float trackX = row.x + 9, trackW = row.w - 22;
        float frac = MathHelper.clamp((float) (mouseX - trackX) / trackW, 0, 1);
        float v = value.getMin() + frac * range(value);
        if (value.isInteger()) v = Math.round(v);
        value.setValue(v);
    }

    private SettingRow settingRowFor(ValueSetting value) {
        for (SettingRow row : settingRows) if (row.setting == value) return row;
        return null;
    }

    private static void step(ValueSetting value, int delta) {
        int min = (int) value.getMin(), max = (int) value.getMax();
        value.setValue(MathHelper.clamp((int) value.getValue() + delta, min, max));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            offsetX = dragStartX + (float) (mouseX - dragMouseX);
            offsetY = dragStartY + (float) (mouseY - dragMouseY);
            return true;
        }
        if (draggingSetting != null) {
            applySlider(draggingSetting, mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        draggingSetting = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        float bgX = window.getScaledWidth() / 2f - BG_W / 2f + offsetX;
        float bgY = window.getScaledHeight() / 2f - BG_H / 2f + offsetY;
        if (MathUtil.isHovered(mouseX, mouseY, bgX + 218f, bgY + 38f, 172f, BG_H - 46f)) {
            settingScroll -= (float) vertical * 16;
            return true;
        }
        if (MathUtil.isHovered(mouseX, mouseY, bgX + 92f, bgY + 38f, 120f, BG_H - 46f)) {
            moduleScroll += (float) vertical * 16;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchActive) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                searchActive = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchText.isEmpty()) searchText = searchText.substring(0, searchText.length() - 1);
                moduleScroll = 0;
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            SoundManager.playSound(SoundManager.CLOSE_GUI);
            animation.setDirection(BACKWARDS);
            return true;
        }
        int key = keyCode == GLFW.GLFW_KEY_DELETE ? GLFW.GLFW_KEY_UNKNOWN : keyCode;
        if (bindingModule != null) {
            bindingModule.setKey(key);
            bindingModule = null;
            return true;
        }
        if (settingsBinding && selectedModule != null) {
            selectedModule.setKey(key);
            settingsBinding = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchActive && chr >= 32 && chr != 127) {
            searchText += chr;
            moduleScroll = 0;
            return true;
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
            bindingModule = null;
            settingsBinding = false;
            super.close();
        }
    }

    private static final class SettingRow {
        final Setting setting;
        final float x, y, w, h;

        SettingRow(Setting setting, float x, float y, float w, float h) {
            this.setting = setting;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private record Row(Module module, float x, float y, float w, float h) {
        boolean contains(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }
}
