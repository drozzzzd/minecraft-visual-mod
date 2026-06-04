# Torov Visual — порт меню (ClickGUI) из zenith

## Задача
Взять новое меню (ClickGUI) из архива `zenith (1).rar`, заменить им старое меню powder ("powder 2025.1"),
переименовать всё zenith → Torov Visual, проверить и скомпилировать. По уточнению пользователя: переносится
**только меню** (рендер/шрифты/анимации/компоненты), отвязанное от ядра zenith, с адаптером к `powder.addonSystem`;
powder сохраняется. Пакет нового кода — `torovvisual`, отображаемое имя — "Torov Visual".

## Что сделано
- Распакован `zenith (1).rar` (7-Zip), изучены обе архитектуры. Установлено: меню zenith жёстко зависит от
  `ru.zenith.core.Main` (всё ядро), поэтому перенесён **визуальный срез** меню, а ядро заменено лёгким фасадом.
- Вычислено транзитивное замыкание меню; перенесено ~90 файлов под `torovvisual` (пакет `ru.zenith`→`torovvisual`,
  `ru.kotopushka` оставлен как сторонний SDK). Подсистемы: меню (37 компонентов), shape (Rectangle/Blur/Arc/Image),
  draw, font, animation, sound, модель модулей/настроек, нужные util (color/math/render/entity/world).
- Отвязка от ядра zenith:
  - создан фасад `torovvisual.core.Main` (отдаёт `ScissorManager`, `ModuleRepository`, `ModuleProvider`);
  - `Module` очищен от EventManager/Hud/Notifications — `activate()/deactivate()` стали точками расширения;
  - `Fonts` грузит ресурсы через свой classloader; `FontRenderer` без TextFactoryEvent; `ColorUtil.getClientColor()`
    — константный акцент `0xFF6C9AFD`; `UserComponent` без Discord; утилиты `PlayerInventoryComponent`/
    `PlayerIntersectionUtil`/`ServerUtil` урезаны до нужного меню.
- Адаптер к powder (`torovvisual.adapter`):
  - `ModuleRepository` строит список модулей из `Powder.addonSystem.getModules()`;
  - `PowderModule` оборачивает `Addon` (toggle → `addonSystem.logic.toggleModule`, категория из `Type`);
  - `PowderBooleanSetting`/`PowderValueSetting` — двусторонние мосты к `CheckBoxWidget`/`SliderWidget`.
- Открытие меню: `powder.client.handler.Keyboard` (RIGHT_SHIFT) → `MenuScreen.INSTANCE.openGui()`.
- Удалён старый ClickGUI: `powder/client/gui/screens/clickGui/**`.
- Сборка: в `build.gradle` добавлен lombok (compileOnly+annotationProcessor) и `accessWidenerPath`;
  `torov.accesswidener` (из zenith) + запись в `fabric.mod.json`. Ресурсы (шрифты/шейдеры core/текстуры)
  скопированы в `assets/minecraft`.
- Ребрендинг: namespace динамических текстур шрифта `zenith`→`torovvisual`; комментарии очищены. Имя мода уже было
  "Torov Visual".

## Изменённые/новые файлы (ключевое)
- Новое: `torovvisual/**` (~92 .java), `ru/kotopushka/**` (6), `torovvisual/core/Main.java`,
  `torovvisual/api/feature/module/ModuleRepository.java`, `torovvisual/adapter/{PowderModule,PowderBooleanSetting,PowderValueSetting}.java`.
- Правки powder: `build.gradle`, `src/main/resources/fabric.mod.json`, `src/main/java/powder/client/handler/Keyboard.java`.
- Ресурсы: `assets/minecraft/{fonts,shaders,textures}/*`, `torov.accesswidener`.
- Удалено: `powder/client/gui/screens/clickGui/**`.

## Тесты/сборка
- JDK 21 (`C:\Users\mi\.jdks\jdk-21.0.11+10`).
- `gradlew compileJava --offline` — PASS (лог `.logs/compile4.txt`).
- `gradlew clean build --offline` — PASS, BUILD SUCCESSFUL (лог `.logs/build_clean.txt`).
- Проверка jar `build/libs/torov-visual-1.0-1.21.4.jar`: 148 классов `torovvisual/`, ресурсы и `torov.accesswidener`
  на месте; старого `clickGui` — 0 записей.

## НЕ менялось / границы
- Логика powder-аддонов, события, рендер powder, прочие экраны (draggableGui) не трогались.
- Боевые/мув модули zenith (killaura и т.п.), команды, Discord, файловая/конфиг-система zenith — НЕ переносились.

## Риски / следующий шаг
1. **Runtime в игре не проверялся** (нужен запуск Minecraft) — гарантирована только компиляция/сборка.
   Основной риск: ленивая загрузка кастомных core-шейдеров (round/blur/arc) в 1.21.4.
2. Категории меню Combat/Movement/Player пусты — powder использует таксономию `Type{DRAG,UTILS,HUD,VISUAL}`
   (модули попадают в Render/Misc). При желании — донастроить маппинг категорий.
3. Настройки-слайдеры показываются как float (powder `SliderWidget` без флага integer).
4. Следующий шаг: `gradlew runClient` для живой проверки меню (RIGHT_SHIFT) и рендера шейдеров.
