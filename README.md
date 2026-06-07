# Torov Visual

A **Minecraft 1.21.4 (Fabric)** client-side visual mod — a Rich-Modern–style ClickGUI, a custom
main menu + loading screen, an MSDF-font HUD suite and world/render effects.

> Архитектура: ядро **Powder** (модули, события, виджеты, рендер, MSDF-шрифты) + визуальный слой
> **Torov Visual** (ClickGUI, HUD-элементы, шрифты, фигуры, шейдеры). Всё собирается в **один jar**.

| | |
|---|---|
| **Minecraft** | 1.21.4 |
| **Loader** | Fabric `0.16.10` |
| **Fabric API** | `0.115.1+1.21.4` |
| **Java** | 21 (требуется для сборки) |
| **Artifact** | `torov-visual-1.1-1.21.4.jar` |

> Версия мода **бампается на каждом шаге** (`gradle.properties` → `mod_version`); старые jar
> остаются в `build/libs/` как бекап.

---

## 🎛️ ClickGUI (Right Shift)

Меню в стиле **Rich-Modern**, переписанное под 1.21.4 на наших MSDF-шрифтах/иконках. Серо-белая
тема, левая колонка категорий с иконками, шапка, список модулей и панель настроек.

- **Разделы:** **Render**, **Movement**, **Hud** (модули берутся из `AddonSystem`).
- **Рабочий поиск** — печатаешь в строке справа сверху, фильтрует модули по всем разделам.
- **Тултипы настроек** — описание/имя при наведении.
- **Тема-пикер** — палитра client-color внизу колонки (меняет акцент GUI/HUD вживую).
- **Outline/Glow текста** — через MSDF-шейдер `msdf_font` (бренд, выбранная категория, заголовки).
- Перетаскивание панели (средняя кнопка), open/close-анимация, бинды модулей.

**MSDF-шрифты:** атласы (`bold/regular/categoryicons/guiicons/hudicons/icons/mainmenuicons`) в
`assets/mre/fonts/`, рендер через `powder.api.render.msdf` (`msdf_font` шейдер) и фасад
`torovvisual.api.system.font.RichFonts`.

## 🏠 Главное меню + загрузка

- **LoadingScreen** — брендированный сплеш при старте (лого + glow-заголовок + анимированный
  прогресс-бар), затем переходит в меню.
- **MainMenu** — лого-картинка, анимированные боковые панели, glow-заголовок «Torov Visual»,
  часы, кнопки (Singleplayer / Multiplayer / AltManager / Options-Quit), футер
  «Torov Visual © All Rights Reserved».

---

## ✨ Возможности

### 🖥️ HUD (`torovvisual.adapter.hud`)
Каждый HUD-элемент — модуль категории **Hud**; многие **перетаскиваются в открытом чате**.
Переоформлены под Rich-Modern (карточки-градиенты, бейджи, чипы) на MSDF-шрифтах:

| Модуль | Описание |
|--------|----------|
| **HotKeys** | «Binds»: список активных биндов + бейдж счётчика |
| **Potions** | Эффекты: иконка + имя + `LVL n` + чип таймера |
| **TargetHud** | Цель: лицо, имя, HP-строка, анимированный health-бар + absorption |
| **Cooldowns** | Предметы на кулдауне: иконка + имя + чип времени |
| **BossBars** | Кастомные босс-бары (градиент + цвет босса) |
| **HotBar** | Хотбар: градиент-карточка, белая обводка выбранного слота |
| **Watermark / DynamicIsland / Armor / PlayerInfo / ScoreBoard / Notifications** | Прочие элементы |

### 🎨 Визуальные эффекты (`powder.client.addon.addons.visual`) — раздел **Render**

| Модуль | Описание |
|--------|----------|
| **GlassHands** | Стеклянные руки от 1-го лица: тинт + полупрозрачность |
| **ChunkAnimation** | Анимация появления чанков *(модуль/настройка есть; in-world хук под 1.21.4 в работе — исходник 1.21.11-only)* |
| **Sounds** | Звук при вкл/выкл модулей: наборы New / Old + громкость |
| **SwingAnimation** | Кастомный взмах, 15 модов (вкл. Chop, Smooth 2, Swipe, Spin…) |
| **ChinaHat** | Китайская шляпа над головой |
| **JumpCircle** | Светящееся кольцо при прыжке |
| **TargetESP** | Подсветка цели |
| **CustomHand** | Перекраска руки: цвет клиента / hue / радуга |
| **BlockOverlay** | Выделение блока GLSL-шейдером: Nebula / Cobweb / Plasma |
| **HitboxCustomizer / WorldParticles / CustomSky / Gamma / ColorCorrection** | Прочие эффекты |

### 🏃 Movement
| Модуль | Описание |
|--------|----------|
| **AutoSprint** | Авто-спринт |

---

## 🔧 Сборка

> На машине по умолчанию `java` — Java 8, её отвергает fabric-loom. Сборка под **JDK 21**.

```powershell
$env:JAVA_HOME = "C:\Users\mi\.jdks\jdk-21.0.11+10"
.\gradlew build
```

Готовый мод: `build/libs/torov-visual-1.1-1.21.4.jar` — в папку `mods`.
Файл `*-sources.jar` рядом — только исходники, в `mods` не нужен. Сборка обфусцируется ProGuard
(class-name-only). После правки `proguard.pro` собирать с `--rerun-tasks`.

---

## 🧱 Как это устроено

- **Модули** (`powder.client.addon`): функция — `Addon` с типом (HUD / VISUAL / MOVEMENT / UTILS),
  виджетами (`@IWidget`: слайдеры, чекбоксы) и подпиской на события. В ClickGUI они показываются
  через адаптер `torovvisual.adapter.PowderModule` (категории RENDER/MOVEMENT/HUD).
- **События** (`powder.api.event`): `@EventSubscribe`; шина обходит иерархию классов.
- **Миксины** (`powder.client.mixins`): хуки рендера — `EventRender2D/3D`, хотбар/босс-бар,
  контур блока, рука (`MixinHeldItemRenderer`: Swing/CustomHand/GlassHands), тайтл-скрин → меню.
- **Шейдеры** (`assets/mre/shaders/core`): `msdf_font` (MSDF-текст), `round`, `blur`, небо, блок-оверлей.
- **Перетаскивание HUD**: `movable() = true` → `Draggable`; `MixinChatScreen` двигает при открытом чате.

---

## ⚠️ Дисклеймер

Проект учебно-экспериментальный (клиентские визуалы / одиночная игра). На серверах — ответственно
и по их правилам.
