package torovvisual.api.system.font.entry;

import torovvisual.api.system.font.glyph.Glyph;

public record DrawEntry(float atX, float atY, int color, Glyph toDraw) {
}
