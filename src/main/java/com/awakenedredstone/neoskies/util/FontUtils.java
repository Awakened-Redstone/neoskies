package com.awakenedredstone.neoskies.util;

import com.awakenedredstone.neoskies.font.FontManager;
import com.awakenedredstone.neoskies.font.FontProvider;

public class FontUtils {
    public static int getStringWidth(String string) {
        //use font manager to get the width of each character in the string and add them together
        FontManager fontManager = FontManager.INSTANCE;
        int width = 0;

        for (int i = 0; i < string.toCharArray().length; i++) {
            String c = String.valueOf(string.charAt(i));
            for (FontProvider fontProvider : fontManager.fontProviders) {
                if (!fontProvider.advances().isEmpty() && fontProvider.advances().containsKey(c)) {
                    width += fontProvider.advances().get(c);
                    break;
                } else if (!fontProvider.chars().isEmpty() && fontProvider.containsChar(string.charAt(i))) {
                    width += fontProvider.ascent();
                    break;
                } else if (fontProvider.glyphSizes() != null) {
                    width += fontProvider.getGlyphWidth(string.codePointAt(i));
                    break;
                }
            }
        }

        return width;
    }

    public static void visitGlyphs(String string, GlyphVisitor<?> visitor) {
        for (char c : string.toCharArray()) {
            visitor.accept(getGlyphWidth(c), c);
        }
    }

    public static int getGlyphWidth(char ch) {
        FontManager fontManager = FontManager.INSTANCE;
        for (FontProvider fontProvider : fontManager.fontProviders) {
            if (!fontProvider.advances().isEmpty() && fontProvider.advances().containsKey(String.valueOf(ch))) {
                return fontProvider.advances().get(String.valueOf(ch));
            } else if (!fontProvider.chars().isEmpty() && fontProvider.containsChar(ch)) {
                return fontProvider.ascent();
            } else if (fontProvider.glyphSizes() != null) {
                return fontProvider.getGlyphWidth(Character.codePointAt(new char[]{ch}, 0));
            }
        }
        return 0;
    }

    public static int getGlyphWidth(int codePoint) {
        char c = Character.toChars(codePoint)[0];
        FontManager fontManager = FontManager.INSTANCE;
        for (FontProvider fontProvider : fontManager.fontProviders) {
            if (!fontProvider.advances().isEmpty() && fontProvider.advances().containsKey(String.valueOf(c))) {
                return fontProvider.advances().get(String.valueOf(c));
            } else if (!fontProvider.chars().isEmpty() && fontProvider.containsChar(c)) {
                return fontProvider.ascent();
            } else if (fontProvider.glyphSizes() != null) {
                return fontProvider.getGlyphWidth(codePoint);
            }
        }
        return 0;
    }

    public interface GlyphVisitor<T> {
        void accept(int advance, char character);
    }
}
