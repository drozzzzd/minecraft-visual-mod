# Torov Visual — доработка мода

## 1. Задача
Доработать Fabric-мод `torov-visual` (MC 1.21.4): не ставить игру на паузу при открытии click GUI,
убрать Aura + добавить FPS в HUD, переделать раскладку click GUI (без наложений, скролл, раскрытие по ПКМ),
добавить фичу Custom Sky, заменить Target Info, обфусцировать/защитить jar и собрать мод.

## 2. Что сделано
- **Игра не встаёт на паузу.** `ClickGui` и `DraggableGui` переопределяют `shouldPause()` → `false`. Мир продолжает тикать как до открытия меню.
- **Удалена функция Aura.** Удалён `Aura.java`, убраны все ссылки (`AddonSystem`, `TargetESP`, `SwingAnimation` — включая зависимый чекбокс «Only Aura»).
- **FPS в HUD.** Новый аддон `Fps` в том же стиле, что и остальные HUD-элементы (blur-подложка + текст), draggable, через `EventRender2D`.
- **Раскладка click GUI переписана (`AddonsElement`):**
  - 2 колонки, накопительное смещение по Y в каждой колонке → функции и их настройки больше не наезжают друг на друга; при раскрытии одной соседние отодвигаются.
  - Прокрутка колёсиком мыши (`ClickGui.mouseScrolled` → `AddonsElement.scroll`), с клампом.
  - Контент обрезается scissor-ом по области GUI → ничего не вылезает за пределы меню.
  - Настройки скрыты по умолчанию: видно только имя+тумблер; настройки раскрываются **правой кнопкой** по строке функции (ЛКМ — тумблер вкл/выкл и работа с виджетами).
  - Добавлено поле `Addon.expanded`; helper `Draggable.byName(...)`.
- **Custom Sky (раздел visual).** Новый аддон `CustomSky` + миксин `MixinSky` (отмена ванильного `renderSky`/`renderClouds`). Логика шейдера портирована из присланного `ShaderFogModule` на API powder (`ResourceProvider`, `EventRender3D`). Настройки: режим Water/Caustic (чекбокс), Speed, Scale, Intensity, Alpha. Добавлены шейдеры под namespace `mre`: `assets/mre/shaders/core/sky/{sky.vsh,water.fsh/json,caustic.fsh/json}` (отсутствовавший в архиве vertex-шейдер создан заново).
- **Target Info заменён.** `TargetInfo` переписан в полноценный HUD-аддон по присланному `TargetInfoWidget` (голова игрока, скроллящийся ник, текст HP, градиентный HP-бар + absorption, fade-анимация), портирован на API powder. Цель берётся **по прицелу (raycast)** — выбор согласован, т.к. Aura удалена.
- **Обфускация/защита (лёгкая безопасная):**
  - Сборка со снятием debug-информации (`options.debug=false`, `debugLevel=none`) — байткод хуже читается при декомпиляции.
  - Runtime integrity self-check (`Protection`): при старте пересчитывает SHA-256 по своим классам `powder/**.class` и сверяет с `protection.key`, вшитым в jar при релизе. При модификации классов мод не инициализирует функционал. В dev-окружении (классы из папки) проверка пропускается.
  - Лёгкая обфускация строк (XOR) для имени ключевого ресурса.
  - Пост-обработка jar: `tools/protect.ps1` считает хеш и вписывает его в `protection.key` без перекомпиляции (гарантия совпадения).
  - Примечание: полное переименование классов через ProGuard не делалось намеренно — оно ломает Fabric-миксины/entrypoints; выбран безопасный вариант (мод гарантированно рабочий).

## 3. Изменённые / новые файлы
Изменены: `ClickGui.java`, `DraggableGui.java`, `Draggable.java`, `Addon.java`, `AddonsElement.java`,
`AddonSystem.java`, `TargetESP.java`, `SwingAnimation.java`, `TargetInfo.java`, `Powder.java`, `build.gradle`, `fabric.mixin.json`.
Новые: `Fps.java`, `CustomSky.java`, `MixinSky.java`, `Protection.java`, `protection.key`,
шейдеры `assets/mre/shaders/core/sky/*`, `tools/protect.ps1`.
Удалён: `Aura.java`.

## 4. Сборка / проверки
- Сборка: `gradlew build --offline` (JDK 21: `C:\Users\mi\.jdks\jdk-21.0.11+10`) → **BUILD SUCCESSFUL** (лог: `.logs/build_offline.txt`).
- Integrity: хеш совпал (stored == computed) — проверено отдельным скриптом. PASS.
- Игровой runtime-тест (запуск MC-клиента) **не выполнялся** в этой среде — требуется ручной запуск.

## 5. Артефакт
- Финальный защищённый мод: `C:\Users\mi\Downloads\torov-visual-1.0-1.21.4.jar` (355 974 байта).
- Source-jar намеренно не отдаётся (содержит исходники).
- Commit: не делался (по умолчанию без явного запроса).

## 6. Что НЕ менялось
Прочие аддоны (ChinaHat, JumpCircle, Gamma, Potions, Keybind, Logotype, DynamicIsland, AutoSprint, HitSound),
render-ядро, шрифты, существующие шейдеры — без изменений (кроме точечных правок ссылок на Aura).

## 6a. Исправление после первого запуска (краш)
Симптом: `NullPointerException ... ResourceProvider.RESOURCE_MANAGER is null` при рендере ClickGui (падал шрифт).
Причина: статические `ShaderProgramKey` в `CustomSky` обращались к `ResourceProvider` во время инициализации мода
(CustomSky создаётся в `AddonSystem` → статике `Powder`), из-за чего класс `ResourceProvider` грузился слишком рано и
его `static RESOURCE_MANAGER` захватывал `null`.
Фикс: ключи шейдеров делаются лениво при первом рендере (метод `shaderKey()`), `ResourceProvider` больше не трогается на старте.
Пересобрано → BUILD SUCCESSFUL, integrity пересчитан. Артефакт обновлён.

## 7. Риски / следующий шаг
1. Custom Sky / Target Info проверены только компиляцией — нужен ручной запуск в игре (вкл. Custom Sky, навестись на игрока).
2. `MixinSky` использует `require=0`: при несовпадении маппингов небо просто не отменится (без краша) — проверить визуально.
3. Защита — деривертент клиентского уровня (любая клиентская защита обходится); даёт защиту от простой подмены классов.
4. Ключ `F` у FPS может пересекаться с ванильным «сменить руку» — при необходимости сменить кей.
5. Следующий шаг: запустить `gradlew runClient` и проверить все 4 пункта UI + Custom Sky + Target Info в игре.
