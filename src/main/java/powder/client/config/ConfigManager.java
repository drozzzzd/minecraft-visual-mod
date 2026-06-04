package powder.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import powder.Powder;
import powder.client.addon.Addon;
import powder.client.gui.widget.Widget;
import powder.client.gui.widget.widgets.CheckBoxWidget;
import powder.client.gui.widget.widgets.SliderWidget;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Persists module settings (enabled state, keybind and every widget value) to disk.
 *
 * <p>On launch a {@code config/torovvisual} folder is created; {@code current.cfg}
 * is loaded if present (else an initial one is written), and it is saved again on
 * exit via a shutdown hook. Named configs are managed in chat:
 * <ul>
 *   <li>{@code .cfg save <name>} — save the current settings as {@code <name>};</li>
 *   <li>{@code .cfg del <name>}  — delete {@code <name>};</li>
 *   <li>{@code .cfg load <name>} — apply {@code <name>};</li>
 *   <li>{@code .cfg list}        — list saved configs.</li>
 * </ul>
 */
public final class ConfigManager {

    private static final Path DIR = FabricLoader.getInstance().getGameDir().resolve("config").resolve("torovvisual");
    private static final Path CURRENT = DIR.resolve("current.cfg");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static boolean initialized = false;

    private ConfigManager() {
    }

    /** Called once at client init: create folder, load (or create) current.cfg, register the exit save. */
    public static void init() {
        if (initialized) return;
        initialized = true;
        try {
            Files.createDirectories(DIR);
            if (Files.exists(CURRENT)) {
                load(CURRENT);
                Powder.LOGGER.info("Config: {}", "loaded current config");
            } else {
                save(CURRENT);
                Powder.LOGGER.info("Config: {}", "created initial config at " + CURRENT);
            }
        } catch (Throwable e) {
            Powder.LOGGER.error("Config init failed", e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                save(CURRENT);
            } catch (Throwable ignored) {
            }
        }, "TorovVisual-ConfigSave"));
    }

    public static boolean saveNamed(String name) {
        try {
            Files.createDirectories(DIR);
            save(DIR.resolve(sanitize(name) + ".cfg"));
            save(CURRENT);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean deleteNamed(String name) {
        try {
            return Files.deleteIfExists(DIR.resolve(sanitize(name) + ".cfg"));
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean loadNamed(String name) {
        Path p = DIR.resolve(sanitize(name) + ".cfg");
        if (!Files.exists(p)) return false;
        try {
            load(p);
            save(CURRENT);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static List<String> list() {
        try (Stream<Path> s = Files.list(DIR)) {
            return s.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".cfg") && !n.equals("current.cfg"))
                    .map(n -> n.substring(0, n.length() - 4))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            return Collections.emptyList();
        }
    }

    // ── Serialization ───────────────────────────────────────────────────────────
    private static void save(Path path) throws Exception {
        JsonObject root = new JsonObject();
        for (Addon addon : Powder.addonSystem.getModules()) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", addon.isEnable());
            o.addProperty("bind", addon.getKey());

            JsonArray widgets = new JsonArray();
            for (Widget w : addon.widgets) {
                if (w instanceof SliderWidget slider) widgets.add(slider.currentValue);
                else if (w instanceof CheckBoxWidget cb) widgets.add(cb.isActive);
            }
            o.add("widgets", widgets);
            root.add(addon.getName(), o);
        }
        Files.writeString(path, GSON.toJson(root));
    }

    private static void load(Path path) throws Exception {
        JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        for (Addon addon : Powder.addonSystem.getModules()) {
            if (!root.has(addon.getName())) continue;
            JsonObject o = root.getAsJsonObject(addon.getName());

            if (o.has("widgets")) {
                JsonArray widgets = o.getAsJsonArray("widgets");
                int i = 0;
                for (Widget w : addon.widgets) {
                    if (i >= widgets.size()) break;
                    JsonElement el = widgets.get(i++);
                    try {
                        if (w instanceof SliderWidget slider) slider.currentValue = el.getAsFloat();
                        else if (w instanceof CheckBoxWidget cb) cb.isActive = el.getAsBoolean();
                    } catch (Throwable ignored) {
                    }
                }
            }
            if (o.has("bind")) addon.setKey(o.get("bind").getAsInt());
            if (o.has("enabled")) {
                boolean want = o.get("enabled").getAsBoolean();
                if (want != addon.isEnable()) Powder.addonSystem.logic.toggleModule(addon);
            }
        }
    }

    // ── Chat command ────────────────────────────────────────────────────────────
    public static void handleCommand(String raw) {
        String[] parts = raw.trim().split("\\s+");
        if (parts.length < 2) {
            info("Usage: §b.cfg <save|del|load|list> [name]");
            return;
        }
        String sub = parts[1].toLowerCase();
        switch (sub) {
            case "save" -> {
                if (parts.length < 3) { info("§cUsage: .cfg save <name>"); return; }
                info(saveNamed(parts[2]) ? "§aSaved config '" + parts[2] + "'" : "§cFailed to save");
            }
            case "del", "delete" -> {
                if (parts.length < 3) { info("§cUsage: .cfg del <name>"); return; }
                info(deleteNamed(parts[2]) ? "§aDeleted config '" + parts[2] + "'" : "§cNo such config");
            }
            case "load" -> {
                if (parts.length < 3) { info("§cUsage: .cfg load <name>"); return; }
                info(loadNamed(parts[2]) ? "§aLoaded config '" + parts[2] + "'" : "§cNo such config");
            }
            case "list" -> {
                List<String> l = list();
                info(l.isEmpty() ? "No saved configs" : "Configs: §b" + String.join("§r, §b", l));
            }
            default -> info("Usage: §b.cfg <save|del|load|list> [name]");
        }
    }

    private static void info(String message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.inGameHud != null) {
            mc.inGameHud.getChatHud().addMessage(Text.of("§7[§bTorov§7] §r" + message));
        }
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
