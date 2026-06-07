package torovvisual.implement.screens.menu.mainmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import torovvisual.api.system.font.RichFonts;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.common.QuickImports;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.math.MathUtil;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Custom Torov Visual main menu shown in place of the vanilla title screen.
 * Background image + animated dim overlay, gradient title, live clock and a
 * column of buttons (Singleplayer, Multiplayer, AltManager, Options/Quit).
 */
public class MainMenu extends Screen implements QuickImports {

    /** Master toggle used by the title-screen mixin. */
    public static boolean ENABLED = true;

    private static final String BACKGROUND = "textures/mainmenu_bg.png";
    private static final String TITLE = "Torov Visual";

    private Button singleplayer, multiplayer, altManager;
    private SplitButton optionsQuit;

    private float fadeIn = 0f;
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    public MainMenu() {
        super(Text.literal(TITLE));
    }

    @Override
    protected void init() {
        fadeIn = 0f;
        particles.clear();
        for (int i = 0; i < 45; i++) particles.add(new Particle(true));

        int w = 200, h = 26;
        singleplayer = new Button("Singleplayer", w, h);
        multiplayer = new Button("Multiplayer", w, h);
        altManager = new Button("AltManager", w, h);
        optionsQuit = new SplitButton("Options", "Quit", w, h);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // background drawn manually in render()
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        RichFonts.begin(matrix);
        if (fadeIn < 1f) fadeIn = Math.min(1f, fadeIn + 0.04f);

        // Background image (stretched full screen) + dim overlay for contrast.
        image.setTexture(BACKGROUND).render(ShapeProperties.create(matrix, 0, 0, this.width, this.height)
                .color(0xFFFFFFFF).build());
        rectangle.render(ShapeProperties.create(matrix, 0, 0, this.width, this.height)
                .color(ColorUtil.getColor(8, 8, 12, 150)).build());

        for (Particle particle : particles) {
            particle.update();
            particle.render(matrix);
        }

        float ease = easeOut(fadeIn);

        // Animated side panels (slide in from the edges).
        float panelW = 150, panelH = this.height * 0.6f;
        float panelY = this.height / 2f - panelH / 2f;
        float lx = -panelW + (panelW + 12) * ease;
        float rx = this.width - 12 - panelW * ease;
        for (float[] p : new float[][]{{lx, panelY}, {rx, panelY}}) {
            rectangle.render(ShapeProperties.create(matrix, p[0], p[1], panelW, panelH).round(10)
                    .thickness(1).softness(1).outlineColor(ColorUtil.multAlpha(theme(), 0.30F))
                    .color(ColorUtil.getColor(16, 16, 20, 120)).build());
        }

        // Logo above the title.
        float logoW = 150, logoH = 48;
        float logoX = (this.width - logoW) / 2f;
        float logoY = this.height / 6f - 44;
        MathUtil.setAlpha(fadeIn, () -> image.setTexture("textures/torov_logo.png")
                .render(ShapeProperties.create(matrix, logoX, logoY, logoW, logoH).color(0xFFFFFFFF).build()));

        // Title (source MSDF font) with a theme-coloured glow.
        float titleSize = 30;
        float titleY = this.height / 6f;
        RichFonts.BOLD.drawCenteredGlow(TITLE, this.width / 2f, titleY, titleSize, 0xFFFFFFFF,
                ColorUtil.multAlpha(theme(), 0.45F));

        // Live clock under the title.
        LocalTime now = LocalTime.now();
        String clock = String.format("%02d:%02d:%02d", now.getHour(), now.getMinute(), now.getSecond());
        RichFonts.BOLD.drawCentered(clock, this.width / 2f, titleY + titleSize + 6, 12,
                ColorUtil.getColor(220, 220, 230, 230));

        // Button column, centred.
        int centerX = this.width / 2 - 100;
        float startY = titleY + titleSize + 32;
        int step = 26 + 8;

        singleplayer.set(centerX, (int) startY);
        multiplayer.set(centerX, (int) (startY + step));
        altManager.set(centerX, (int) (startY + step * 2));
        optionsQuit.set(centerX, (int) (startY + step * 3));

        matrix.push();
        MathUtil.setAlpha(fadeIn, () -> {
            singleplayer.render(matrix, mouseX, mouseY);
            multiplayer.render(matrix, mouseX, mouseY);
            altManager.render(matrix, mouseX, mouseY);
            optionsQuit.render(matrix, mouseX, mouseY);
        });
        matrix.pop();

        // Version label bottom-right.
        String version = "Torov Visual " + powder.Powder.session;
        float vWidth = RichFonts.BOLD.getWidth(version, 7);
        RichFonts.BOLD.draw(version, this.width - vWidth - 6, this.height - 12, 7, ColorUtil.getColor(180, 180, 190, 200));

        // Footer (rebranded from "Rich Client © All Rights Reserved").
        RichFonts.BOLD.drawCentered("Torov Visual © All Rights Reserved", this.width / 2f, this.height - 9, 6,
                new java.awt.Color(128, 128, 128, 160).getRGB());

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (singleplayer.hovered(mouseX, mouseY)) {
                mc.setScreen(new SelectWorldScreen(this));
                return true;
            }
            if (multiplayer.hovered(mouseX, mouseY)) {
                mc.setScreen(new MultiplayerScreen(this));
                return true;
            }
            if (altManager.hovered(mouseX, mouseY)) {
                mc.setScreen(new AltManager(this));
                return true;
            }
            if (optionsQuit.optionHovered(mouseX, mouseY)) {
                mc.setScreen(new OptionsScreen(this, mc.options));
                return true;
            }
            if (optionsQuit.quitHovered(mouseX, mouseY)) {
                mc.scheduleStop();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static int theme() {
        return ColorUtil.getClientColor();
    }

    private static float easeOut(float t) {
        return 1f - (float) Math.pow(1 - t, 3);
    }

    // ── Background particle ──────────────────────────────────────────────────
    private class Particle {
        float x, y, vx, vy, size;
        int alpha;

        Particle(boolean spread) {
            reset();
            if (spread) y = random.nextFloat() * Math.max(1, height);
        }

        void reset() {
            x = random.nextFloat() * Math.max(1, width);
            y = -10;
            vx = (random.nextFloat() - 0.5f) * 0.4f;
            vy = random.nextFloat() * 0.4f + 0.2f;
            size = random.nextFloat() * 1.6f + 0.8f;
            alpha = random.nextInt(90) + 40;
        }

        void update() {
            x += vx;
            y += vy;
            if (y > height + 10 || x < -10 || x > width + 10) reset();
        }

        void render(MatrixStack matrix) {
            rectangle.render(ShapeProperties.create(matrix, x, y, size, size).round(size / 2f)
                    .color(ColorUtil.multAlpha(ColorUtil.getClientColor(), alpha / 255f)).build());
        }
    }

    // ── Single button ───────────────────────────────────────────────────────
    private class Button {
        final String name;
        final int width, height;
        int x, y;
        float hover = 0f;

        Button(String name, int width, int height) {
            this.name = name;
            this.width = width;
            this.height = height;
        }

        void set(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void render(MatrixStack matrix, int mouseX, int mouseY) {
            boolean h = hovered(mouseX, mouseY);
            hover = MathUtil.interpolateSmooth(8, hover, h ? 1f : 0f);

            int base = ColorUtil.getColor(22, 22, 28, 150);
            int hot = ColorUtil.overCol(base, ColorUtil.multAlpha(ColorUtil.getClientColor(), 0.45f), hover);
            rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(6)
                    .thickness(1).softness(1)
                    .outlineColor(ColorUtil.overCol(ColorUtil.getOutline(), ColorUtil.getClientColor(), hover))
                    .color(hot).build());

            RichFonts.BOLD.drawCentered(name, x + width / 2f, y + (height - 8) / 2f, 7, 0xFFFFFFFF);
        }

        boolean hovered(double mouseX, double mouseY) {
            return MathUtil.isHovered(mouseX, mouseY, x, y, width, height);
        }
    }

    // ── Two half-width buttons sharing one row ──────────────────────────────
    private class SplitButton {
        final String left, right;
        final int width, height;
        int x, y;
        float hoverL = 0f, hoverR = 0f;

        SplitButton(String left, String right, int width, int height) {
            this.left = left;
            this.right = right;
            this.width = width;
            this.height = height;
        }

        void set(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void render(MatrixStack matrix, int mouseX, int mouseY) {
            int gap = 4;
            int half = (width - gap) / 2;

            hoverL = MathUtil.interpolateSmooth(8, hoverL, optionHovered(mouseX, mouseY) ? 1f : 0f);
            hoverR = MathUtil.interpolateSmooth(8, hoverR, quitHovered(mouseX, mouseY) ? 1f : 0f);

            drawHalf(matrix, x, half, left, hoverL);
            drawHalf(matrix, x + half + gap, half, right, hoverR);
        }

        private void drawHalf(MatrixStack matrix, int bx, int bw, String text, float hover) {
            int base = ColorUtil.getColor(22, 22, 28, 150);
            int hot = ColorUtil.overCol(base, ColorUtil.multAlpha(ColorUtil.getClientColor(), 0.45f), hover);
            rectangle.render(ShapeProperties.create(matrix, bx, y, bw, height).round(6)
                    .thickness(1).softness(1)
                    .outlineColor(ColorUtil.overCol(ColorUtil.getOutline(), ColorUtil.getClientColor(), hover))
                    .color(hot).build());
            RichFonts.BOLD.drawCentered(text, bx + bw / 2f, y + (height - 8) / 2f, 7, 0xFFFFFFFF);
        }

        boolean optionHovered(double mouseX, double mouseY) {
            int half = (width - 4) / 2;
            return MathUtil.isHovered(mouseX, mouseY, x, y, half, height);
        }

        boolean quitHovered(double mouseX, double mouseY) {
            int half = (width - 4) / 2;
            return MathUtil.isHovered(mouseX, mouseY, x + half + 4, y, half, height);
        }
    }
}
