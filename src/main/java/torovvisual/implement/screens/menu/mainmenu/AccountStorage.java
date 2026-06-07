package torovvisual.implement.screens.menu.mainmenu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persists the offline (cracked) account names used by the AltManager and
 * swaps the running Minecraft session to a chosen account.
 *
 * <p>Names are stored in {@code config/torovvisual/accounts.json}. Switching an
 * account replaces {@link MinecraftClient#session} with an offline session
 * (UUID derived the same way the vanilla server does for offline players), so
 * the new username is used on the next server join.
 */
public final class AccountStorage {

    private static final Path FILE = FabricLoader.getInstance().getGameDir()
            .resolve("config").resolve("torovvisual").resolve("accounts.json");

    private static final List<String> ACCOUNTS = new ArrayList<>();
    private static boolean loaded = false;

    private AccountStorage() {
    }

    public static List<String> getAccounts() {
        if (!loaded) load();
        return ACCOUNTS;
    }

    public static void add(String name) {
        if (name == null || name.isBlank()) return;
        if (getAccounts().stream().noneMatch(a -> a.equalsIgnoreCase(name))) {
            ACCOUNTS.add(name);
            save();
        }
    }

    public static void remove(String name) {
        getAccounts().removeIf(a -> a.equalsIgnoreCase(name));
        save();
    }

    public static void clear() {
        getAccounts().clear();
        save();
    }

    /** Replaces the active session with an offline account using the given username. */
    public static void login(String name) {
        if (name == null || name.isBlank()) return;
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        Session session = new Session(name, uuid, "0",
                Optional.empty(), Optional.empty(), Session.AccountType.LEGACY);
        MinecraftClient.getInstance().session = session;
    }

    private static void load() {
        loaded = true;
        try {
            if (!Files.exists(FILE)) return;
            JsonElement root = JsonParser.parseString(Files.readString(FILE));
            if (!root.isJsonArray()) return;
            for (JsonElement el : root.getAsJsonArray()) {
                String name = el.getAsString();
                if (name != null && !name.isBlank()) ACCOUNTS.add(name);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            JsonArray array = new JsonArray();
            for (String name : ACCOUNTS) array.add(name);
            Files.writeString(FILE, array.toString());
        } catch (Throwable ignored) {
        }
    }
}
