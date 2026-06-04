package torovvisual.adapter;

import powder.client.gui.widget.widgets.SliderWidget;
import torovvisual.api.feature.module.setting.implement.ValueSetting;

/**
 * A numeric (stepper) menu setting backed directly by a Powder
 * {@link SliderWidget}; reads and writes go straight to the widget the addon uses.
 * Rendered as a button stepper (no draggable bar); when {@code options} are given
 * the value indexes named modes.
 */
public class PowderValueSetting extends ValueSetting {
    private final SliderWidget widget;

    public PowderValueSetting(SliderWidget widget, String name, String[] options) {
        super(name, "");
        this.widget = widget;
        setOptions(options);
    }

    @Override
    public float getValue() {
        return widget.currentValue;
    }

    @Override
    public ValueSetting setValue(float value) {
        widget.currentValue = value;
        return this;
    }

    @Override
    public float getMin() {
        return widget.min;
    }

    @Override
    public float getMax() {
        return widget.max;
    }
}
