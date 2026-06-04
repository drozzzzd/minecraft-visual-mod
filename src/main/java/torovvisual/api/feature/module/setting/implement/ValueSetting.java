package torovvisual.api.feature.module.setting.implement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import torovvisual.api.feature.module.setting.Setting;

import java.util.function.Supplier;

@Getter
@Setter
@Accessors(chain = true)
public class ValueSetting extends Setting {
    private float value, min, max;
    private boolean integer;

    /** Optional named options; when present the value is a 0-based index into these labels. */
    private String[] options;

    public ValueSetting(String name, String description) {
        super(name, description);
    }

    public boolean hasOptions() {
        return options != null && options.length > 0;
    }

    public ValueSetting range(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public ValueSetting range(int min, int max) {
        this.min = min;
        this.max = max;
        this.integer = true;
        return this;
    }

    public int getInt() {
        return (int) value;
    }

    public ValueSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }
}