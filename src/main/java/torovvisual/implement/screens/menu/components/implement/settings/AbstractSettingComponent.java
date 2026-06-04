package torovvisual.implement.screens.menu.components.implement.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import torovvisual.api.feature.module.setting.Setting;
import torovvisual.implement.screens.menu.components.AbstractComponent;

@Getter
@RequiredArgsConstructor
public abstract class AbstractSettingComponent extends AbstractComponent {
    private final Setting setting;
}
