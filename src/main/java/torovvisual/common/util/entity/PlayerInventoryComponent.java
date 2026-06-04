package torovvisual.common.util.entity;

import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import torovvisual.common.QuickImports;
import torovvisual.implement.screens.menu.MenuScreen;

import java.util.List;

@UtilityClass
public class PlayerInventoryComponent implements QuickImports {
    public final List<KeyBinding> moveKeys = List.of(mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey);
    public boolean canMove = true;

    public void disableMoveKeys() {
        canMove = false;
        unPressMoveKeys();
    }

    public void unPressMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.setPressed(false));
    }

    public void updateMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyBinding.getDefaultKey().getCode())));
    }

    public boolean shouldSkipExecution() {
        return mc.currentScreen != null && !PlayerIntersectionUtil.isChat(mc.currentScreen) && !(mc.currentScreen instanceof SignEditScreen) && !(mc.currentScreen instanceof AnvilScreen)
                && !(mc.currentScreen instanceof AbstractCommandBlockScreen) && !(mc.currentScreen instanceof StructureBlockScreen) && !(mc.currentScreen instanceof MenuScreen);
    }
}
