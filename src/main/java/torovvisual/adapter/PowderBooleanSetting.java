package torovvisual.adapter;

import powder.client.gui.widget.widgets.CheckBoxWidget;
import torovvisual.api.feature.module.setting.implement.BooleanSetting;

/**
 * A boolean menu setting backed directly by a Powder {@link CheckBoxWidget}, so
 * the value the menu shows and edits is the very same value the addon reads.
 */
public class PowderBooleanSetting extends BooleanSetting {
    private final CheckBoxWidget widget;

    public PowderBooleanSetting(CheckBoxWidget widget) {
        super(widget.name, "");
        this.widget = widget;
    }

    @Override
    public boolean isValue() {
        return widget.isActive;
    }

    @Override
    public BooleanSetting setValue(boolean value) {
        widget.isActive = value;
        return this;
    }
}
