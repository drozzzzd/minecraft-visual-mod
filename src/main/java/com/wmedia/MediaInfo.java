package com.wmedia;

/** Immutable snapshot of the media currently playing on the PC. */
public final class MediaInfo {
    private final String title;
    private final String artist;
    private final boolean playing;
    private final float positionMs;
    private final float durationMs;
    private final byte[] albumArt;

    public MediaInfo(String title, String artist, boolean playing, float positionMs, float durationMs, byte[] albumArt) {
        this.title = title == null ? "" : title;
        this.artist = artist == null ? "" : artist;
        this.playing = playing;
        this.positionMs = positionMs;
        this.durationMs = durationMs;
        this.albumArt = albumArt == null ? new byte[0] : albumArt.clone();
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public boolean isPlaying() {
        return playing;
    }

    public float getPositionMs() {
        return positionMs;
    }

    public float getDurationMs() {
        return durationMs;
    }

    public byte[] getAlbumArt() {
        return albumArt.clone();
    }

    /** 0..1 playback progress. */
    public float getProgress() {
        return durationMs > 0f ? Math.max(0f, Math.min(1f, positionMs / durationMs)) : 0f;
    }
}
