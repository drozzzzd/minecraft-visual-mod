package com.wmedia;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Real implementation backed by {@code dev.redstones.mediaplayerinfo} (Windows
 * SMTC / "now playing"). Reads the currently active media session and exposes it
 * as a plain {@link MediaInfo}. Every call is guarded — if the native bridge or
 * Kotlin runtime is unavailable, it degrades to "no media" instead of crashing.
 */
public final class MediaProvider {

    private MediaProvider() {
    }

    public static Optional<MediaInfo> getCurrentMedia() {
        try {
            IMediaSession session = MediaPlayerInfo.Instance.getMediaSessions().stream()
                    .max(Comparator.comparing(s -> s.getMedia().getPlaying()))
                    .orElse(null);
            if (session == null) return Optional.empty();

            dev.redstones.mediaplayerinfo.MediaInfo m = session.getMedia();
            if (m == null || m.getTitle() == null || m.getTitle().isEmpty()) return Optional.empty();

            return Optional.of(new MediaInfo(
                    m.getTitle(),
                    m.getArtist(),
                    m.getPlaying(),
                    (float) m.getPosition(),
                    (float) m.getDuration(),
                    m.getArtworkPng()
            ));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    public static Optional<AudioLevels> getAudioLevels() {
        return Optional.empty();
    }

    public static boolean isAvailable() {
        try {
            return !MediaPlayerInfo.Instance.getMediaSessions().isEmpty();
        } catch (Throwable t) {
            return false;
        }
    }

    public static void playPause() {
        control(IMediaSession::playPause);
    }

    public static void next() {
        control(IMediaSession::next);
    }

    public static void previous() {
        control(IMediaSession::previous);
    }

    public static void seek(long positionMs) {
        // Not supported by the underlying bridge.
    }

    public static void shutdown() {
    }

    private static void control(Consumer<IMediaSession> action) {
        try {
            MediaPlayerInfo.Instance.getMediaSessions().stream().findFirst().ifPresent(action);
        } catch (Throwable ignored) {
        }
    }
}
