package torovvisual.implement.screens.menu.mainmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import torovvisual.api.system.font.Fonts;
import torovvisual.api.system.shape.ShapeProperties;
import torovvisual.common.QuickImports;
import torovvisual.common.util.color.ColorUtil;
import torovvisual.common.util.math.MathUtil;
import torovvisual.common.util.render.ScissorManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * Offline account manager opened from the {@link MainMenu}. Lets the user
 * store, generate and switch between cracked account names; switching changes
 * the active session via {@link AccountStorage#login(String)}.
 */
public class AltManager extends Screen implements QuickImports {

    private final Screen parent;
    private final ScissorManager scissor = new ScissorManager();
    private final Random random = new Random();

    private boolean typing = false;
    private final StringBuilder input = new StringBuilder();

    private float scroll = 0, targetScroll = 0;
    private int selected = -1;
    private boolean confirmClear = false;

    private static final int ENTRY_HEIGHT = 34;

    public AltManager(Screen parent) {
        super(Text.literal("AltManager"));
        this.parent = parent;
    }

    private List<String> accounts() {
        return AccountStorage.getAccounts();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        scroll = MathUtil.interpolateSmooth(6, scroll, targetScroll);

        // Background image + dim overlay, same look as the main menu.
        image.setTexture("textures/mainmenu_bg.png").render(ShapeProperties.create(matrix, 0, 0, this.width, this.height)
                .color(0xFFFFFFFF).build());
        rectangle.render(ShapeProperties.create(matrix, 0, 0, this.width, this.height)
                .color(ColorUtil.getColor(8, 8, 12, 175)).build());

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Title.
        Fonts.getSize(28, Fonts.Type.BOLD).drawGradientString(matrix, "AltManager",
                centerX - Fonts.getSize(28, Fonts.Type.BOLD).getStringWidth("AltManager") / 2f,
                this.height / 7f, 0xFFFFFFFF, ColorUtil.getClientColor());

        // Input field.
        int inputW = 220, inputH = 20;
        int inputX = centerX - inputW / 2;
        int inputY = centerY - 100;
        rectangle.render(ShapeProperties.create(matrix, inputX, inputY, inputW, inputH).round(5)
                .thickness(1).softness(1).outlineColor(ColorUtil.getOutline())
                .color(ColorUtil.getColor(20, 20, 26, 170)).build());
        String shown;
        if (typing) {
            shown = input + ((System.currentTimeMillis() / 500 % 2 == 0) ? "_" : "");
        } else if (input.length() > 0) {
            shown = input.toString();
        } else {
            StringBuilder ph = new StringBuilder("Enter your name");
            for (int i = 0; i < (System.currentTimeMillis() / 500 % 4); i++) ph.append(".");
            shown = ph.toString();
        }
        Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrix, shown, inputX + 6, inputY + 6,
                ColorUtil.getColor(210, 210, 220, 255));

        // Account list.
        int listX = inputX, listY = centerY - 72, listW = inputW, listH = 150;
        rectangle.render(ShapeProperties.create(matrix, listX, listY, listW, listH).round(5)
                .thickness(1).softness(1).outlineColor(ColorUtil.getOutline())
                .color(ColorUtil.getColor(20, 20, 26, 150)).build());

        scissor.push(new Matrix4f(matrix.peek().getPositionMatrix()), listX, listY, listW, listH);
        List<String> accounts = accounts();
        for (int i = 0; i < accounts.size(); i++) {
            float y = listY + 5 - scroll + i * ENTRY_HEIGHT;
            int entryX = listX + 5;
            int entryW = 150, entryH = 28;

            boolean isSel = i == selected;
            rectangle.render(ShapeProperties.create(matrix, entryX, y, entryW, entryH).round(4)
                    .thickness(1).softness(1)
                    .outlineColor(isSel ? ColorUtil.getClientColor() : ColorUtil.getOutline())
                    .color(ColorUtil.getColor(24, 24, 30, isSel ? 180 : 120)).build());

            Fonts.getSize(13, Fonts.Type.BOLD).drawString(matrix, accounts.get(i), entryX + 7, y + 5,
                    ColorUtil.getColor(210, 210, 220, 255));
            Fonts.getSize(11, Fonts.Type.DEFAULT).drawString(matrix,
                    "Added " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    entryX + 7, y + 17, ColorUtil.getColor(140, 140, 150, 255));

            int btnX = entryX + entryW + 6, btnW = 48, btnH = 12;
            drawMiniButton(matrix, btnX, (int) y, btnW, btnH, "Select",
                    MathUtil.isHovered(mouseX, mouseY, btnX, y, btnW, btnH));
            drawMiniButton(matrix, btnX, (int) y + btnH + 4, btnW, btnH, "Delete",
                    MathUtil.isHovered(mouseX, mouseY, btnX, y + btnH + 4, btnW, btnH));
        }
        scissor.pop();

        // Bottom buttons.
        int buttonsY = listY + listH + 10, bW = 66, bH = 20;
        int createX = centerX - bW - 76;
        int clearX = centerX - bW / 2;
        int randomX = centerX + 76;
        drawButton(matrix, createX, buttonsY, bW, bH, "Create", MathUtil.isHovered(mouseX, mouseY, createX, buttonsY, bW, bH));
        drawButton(matrix, clearX, buttonsY, bW, bH, "Clear all", MathUtil.isHovered(mouseX, mouseY, clearX, buttonsY, bW, bH));
        drawButton(matrix, randomX, buttonsY, bW, bH, "Random", MathUtil.isHovered(mouseX, mouseY, randomX, buttonsY, bW, bH));

        // Info.
        Fonts.getSize(13, Fonts.Type.DEFAULT).drawCenteredString(matrix,
                "Selected account: " + mc.getSession().getUsername(), centerX, buttonsY + bH + 12,
                ColorUtil.getColor(220, 220, 230, 255));
        Fonts.getSize(13, Fonts.Type.DEFAULT).drawCenteredString(matrix,
                "Quantity: " + accounts.size(), centerX, buttonsY + bH + 28,
                ColorUtil.getColor(220, 220, 230, 255));

        if (confirmClear) drawConfirm(matrix);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawButton(MatrixStack matrix, int x, int y, int w, int h, String text, boolean hover) {
        rectangle.render(ShapeProperties.create(matrix, x, y, w, h).round(5).thickness(1).softness(1)
                .outlineColor(hover ? ColorUtil.getClientColor() : ColorUtil.getOutline())
                .color(ColorUtil.getColor(24, 24, 30, hover ? 190 : 140)).build());
        Fonts.getSize(13, Fonts.Type.BOLD).drawCenteredString(matrix, text, x + w / 2f, y + (h - 8) / 2f, 0xFFFFFFFF);
    }

    private void drawMiniButton(MatrixStack matrix, int x, int y, int w, int h, String text, boolean hover) {
        rectangle.render(ShapeProperties.create(matrix, x, y, w, h + 2).round(4)
                .color(ColorUtil.getColor(hover ? 45 : 28, hover ? 45 : 28, hover ? 55 : 36, 170)).build());
        Fonts.getSize(11, Fonts.Type.BOLD).drawCenteredString(matrix, text, x + w / 2f, y + 2, 0xFFFFFFFF);
    }

    private void drawConfirm(MatrixStack matrix) {
        rectangle.render(ShapeProperties.create(matrix, 0, 0, this.width, this.height)
                .color(ColorUtil.getColor(0, 0, 0, 130)).build());
        int boxW = 280, boxH = 120;
        int boxX = (this.width - boxW) / 2, boxY = (this.height - boxH) / 2;
        rectangle.render(ShapeProperties.create(matrix, boxX, boxY, boxW, boxH).round(8).thickness(1).softness(1)
                .outlineColor(ColorUtil.getOutline()).color(ColorUtil.getColor(26, 26, 32, 245)).build());
        Fonts.getSize(14, Fonts.Type.BOLD).drawCenteredString(matrix, "Clear all accounts?",
                this.width / 2f, boxY + 26, 0xFFFFFFFF);

        int btnW = 90, btnH = 26, btnY = boxY + boxH - 42;
        int yesX = boxX + 28, noX = boxX + boxW - 28 - btnW;
        rectangle.render(ShapeProperties.create(matrix, yesX, btnY, btnW, btnH).round(5)
                .color(ColorUtil.getColor(60, 170, 75, 230)).build());
        Fonts.getSize(13, Fonts.Type.BOLD).drawCenteredString(matrix, "Yes", yesX + btnW / 2f, btnY + 8, 0xFFFFFFFF);
        rectangle.render(ShapeProperties.create(matrix, noX, btnY, btnW, btnH).round(5)
                .color(ColorUtil.getColor(190, 60, 60, 230)).build());
        Fonts.getSize(13, Fonts.Type.BOLD).drawCenteredString(matrix, "No", noX + btnW / 2f, btnY + 8, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int centerX = this.width / 2, centerY = this.height / 2;

        if (confirmClear) {
            int boxW = 280, boxH = 120;
            int boxX = (this.width - boxW) / 2, boxY = (this.height - boxH) / 2;
            int btnW = 90, btnH = 26, btnY = boxY + boxH - 42;
            int yesX = boxX + 28, noX = boxX + boxW - 28 - btnW;
            if (MathUtil.isHovered(mouseX, mouseY, yesX, btnY, btnW, btnH)) {
                AccountStorage.clear();
                selected = -1;
                confirmClear = false;
            } else if (MathUtil.isHovered(mouseX, mouseY, noX, btnY, btnW, btnH)) {
                confirmClear = false;
            }
            return true;
        }

        int inputW = 220, inputH = 20;
        int inputX = centerX - inputW / 2, inputY = centerY - 100;
        if (MathUtil.isHovered(mouseX, mouseY, inputX, inputY, inputW, inputH)) {
            typing = true;
            return true;
        }
        typing = false;

        int buttonsY = (centerY - 72) + 150 + 10, bW = 66, bH = 20;
        int createX = centerX - bW - 76, clearX = centerX - bW / 2, randomX = centerX + 76;
        if (MathUtil.isHovered(mouseX, mouseY, createX, buttonsY, bW, bH)) {
            commitInput();
            return true;
        }
        if (MathUtil.isHovered(mouseX, mouseY, clearX, buttonsY, bW, bH)) {
            confirmClear = true;
            return true;
        }
        if (MathUtil.isHovered(mouseX, mouseY, randomX, buttonsY, bW, bH)) {
            String name = randomName();
            AccountStorage.add(name);
            AccountStorage.login(name);
            selected = accounts().indexOf(name);
            return true;
        }

        // List interactions.
        int listX = inputX, listY = centerY - 72, listW = inputW, listH = 150;
        if (MathUtil.isHovered(mouseX, mouseY, listX, listY, listW, listH)) {
            List<String> accounts = accounts();
            for (int i = 0; i < accounts.size(); i++) {
                float y = listY + 5 - scroll + i * ENTRY_HEIGHT;
                int entryX = listX + 5, entryW = 150, entryH = 28;
                int btnX = entryX + entryW + 6, btnW = 48, btnH = 12;
                if (MathUtil.isHovered(mouseX, mouseY, btnX, y, btnW, btnH)
                        || MathUtil.isHovered(mouseX, mouseY, entryX, y, entryW, entryH)) {
                    AccountStorage.login(accounts.get(i));
                    selected = i;
                    return true;
                }
                if (MathUtil.isHovered(mouseX, mouseY, btnX, y + btnH + 4, btnW, btnH)) {
                    AccountStorage.remove(accounts.get(i));
                    if (selected == i) selected = -1;
                    clampScroll();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void commitInput() {
        String name = input.toString().trim();
        if (!name.isEmpty()) {
            AccountStorage.add(name);
            input.setLength(0);
            typing = false;
        }
    }

    private String randomName() {
        String[] words = {"Alex", "Silent", "Cat", "Lone", "Pro", "Ninja", "Shadow", "Fire", "Ice",
                "Dragon", "Wolf", "Storm", "Blade", "Ghost", "Pixel", "Neo", "Cyber", "Echo", "Falcon",
                "Nova", "Phantom", "Spark", "Titan", "Vortex", "Zenith", "Alpha", "Delta"};
        StringBuilder name = new StringBuilder(words[random.nextInt(words.length)]);
        if (random.nextBoolean()) name.append("_").append(words[random.nextInt(words.length)]);
        if (random.nextFloat() < 0.7f) name.append(random.nextInt(1000));
        String result = name.toString();
        if (result.length() > 16) result = result.substring(0, 16);
        return result;
    }

    private void clampScroll() {
        int maxOffset = Math.max(0, accounts().size() * ENTRY_HEIGHT - 150 + 5);
        targetScroll = Math.max(0, Math.min(targetScroll, maxOffset));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        int centerY = this.height / 2;
        int listY = centerY - 72, listH = 150;
        if (mouseY >= listY && mouseY <= listY + listH) {
            targetScroll -= vertical * 24;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                commitInput();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && input.length() > 0) {
                input.deleteCharAt(input.length() - 1);
                return true;
            }
            boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
            if (ctrl && keyCode == GLFW.GLFW_KEY_V) {
                String clip = GLFW.glfwGetClipboardString(mc.getWindow().getHandle());
                if (clip != null) {
                    String filtered = clip.replaceAll("[^\\w]", "");
                    int room = 16 - input.length();
                    if (room > 0) input.append(filtered.length() > room ? filtered.substring(0, room) : filtered);
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (typing && input.length() < 16 && (Character.isLetterOrDigit(chr) || chr == '_')) {
            input.append(chr);
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        mc.setScreen(parent);
    }
}
