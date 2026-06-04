package torovvisual.common.trait;

import torovvisual.api.feature.module.setting.Setting;

public interface Setupable {
    void setup(Setting... settings);
}