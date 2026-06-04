package com.wmedia;

/**
 * Stereo audio output levels (0..1). The underlying media bridge does not expose
 * per-channel levels yet, so {@link MediaProvider#getAudioLevels()} currently
 * returns empty — this type exists so the API surface is complete.
 */
public final class AudioLevels {
    private final float left;
    private final float right;

    public AudioLevels(float left, float right) {
        this.left = left;
        this.right = right;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

    public float getPeak() {
        return Math.max(left, right);
    }
}
