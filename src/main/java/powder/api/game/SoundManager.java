package powder.api.game;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Scaffold sound manager for HitSound. The .ogg assets are not bundled yet —
 * drop them under assets/mre/sounds and register them in sounds.json, then the
 * identifiers below will resolve. Until then playSound is a safe no-op (Minecraft
 * logs a missing-sound warning instead of crashing).
 */
public final class SoundManager {

    public static final Identifier MOAN1    = Identifier.of("mre", "moan1");
    public static final Identifier MOAN2    = Identifier.of("mre", "moan2");
    public static final Identifier MOAN3    = Identifier.of("mre", "moan3");
    public static final Identifier MOAN4    = Identifier.of("mre", "moan4");
    public static final Identifier METALLIC = Identifier.of("mre", "metallic");
    public static final Identifier CRIME    = Identifier.of("mre", "crime");

    // HitSound modes (bundled .ogg under assets/mre/sounds, registered in sounds.json).
    public static final Identifier BELL   = Identifier.of("mre", "bell");
    public static final Identifier BUBBLE = Identifier.of("mre", "bubble");
    public static final Identifier HIT1   = Identifier.of("mre", "hit1");
    public static final Identifier HIT2   = Identifier.of("mre", "hit2");
    public static final Identifier BONK   = Identifier.of("mre", "bonk");

    public static void playSound(Identifier id, float volume, float pitch) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        try {
            SoundEvent event = SoundEvent.of(id);
            mc.getSoundManager().play(PositionedSoundInstance.master(event, pitch, volume));
        } catch (Exception ignored) {
            // sound asset not present yet — scaffold, sound added later
        }
    }

}
