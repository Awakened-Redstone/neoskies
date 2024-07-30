package com.awakenedredstone.neoskies.font;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Deprecated
public class FontProvider {
    public static final Codec<FontProvider> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("type").forGetter(FontProvider::type),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("advances", new HashMap<>()).forGetter(FontProvider::advances),
            Codec.STRING.optionalFieldOf("sizes", "").forGetter(FontProvider::sizes),
            Codec.INT.optionalFieldOf("height", 8).forGetter(FontProvider::height),
            Codec.INT.optionalFieldOf("ascent", 0).forGetter(FontProvider::ascent),
            Codec.list(Codec.STRING).optionalFieldOf("chars", new ArrayList<>()).forGetter(FontProvider::chars)
        ).apply(instance, FontProvider::new));

    //providers is an array of FontProvider, so we need to make a codec for that, CODEC represents a single FontProvider
    public static final Codec<List<FontProvider>> LIST_CODEC = CODEC.listOf().fieldOf("providers").codec();
    private final String type;
    private final Map<String, Integer> advances;
    private final String sizes;
    private final byte @Nullable [] glyphSizes;
    private final int height;
    private final int ascent;
    private final List<String> chars;

    public FontProvider(String type, @Nullable Map<String, Integer> advances, @Nullable String sizes, int height, int ascent, @Nullable List<String> chars) {
        byte[] fontSizes = null;
        this.type = type;
        this.advances = advances;
        this.sizes = sizes;
        this.height = height;
        this.ascent = ascent;
        this.chars = chars;

        if (StringUtils.isNotBlank(sizes)) {
            try (InputStream inputStream = IslandLogic.getResourceManager().open(Identifier.tryParse(this.sizes))) {
                fontSizes = inputStream.readNBytes(65536);
            } catch (IOException e) {
                NeoSkies.LOGGER.error("Cannot load {}, unicode glyphs may not be calcualted correctly", sizes);
                NeoSkies.LOGGER.error("Failed to load font sizes", e);
            }
        }
        this.glyphSizes = fontSizes;
    }

    public int getGlyphWidth(int codePoint) {
        if (glyphSizes == null) {
            return 0;
        }
        if (codePoint < 0 || codePoint >= this.glyphSizes.length) {
            return 0;
        }
        return glyphSizes[codePoint];
    }

    public String type() {
        return type;
    }

    public Map<String, Integer> advances() {
        return advances;
    }

    public String sizes() {
        return sizes;
    }

    public byte @Nullable [] glyphSizes() {
        return glyphSizes;
    }

    public int height() {
        return height;
    }

    public int ascent() {
        return ascent;
    }

    public List<String> chars() {
        return chars;
    }

    public boolean containsChar(char c) {
        if (chars != null) {
            for (String string : chars) {
                if (string.contains(String.valueOf(c))) return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FontProvider) obj;
        return Objects.equals(this.type, that.type) &&
            Objects.equals(this.advances, that.advances) &&
            Objects.equals(this.sizes, that.sizes) &&
            this.height == that.height &&
            this.ascent == that.ascent &&
            Objects.equals(this.chars, that.chars);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, advances, sizes, height, ascent, chars);
    }

    @Override
    public String toString() {
        return "FontProvider[" +
            "type=" + type + ", " +
            "advances=" + advances + ", " +
            "sizes=" + sizes + ", " +
            "height=" + height + ", " +
            "accent=" + ascent + ", " +
            "chars=" + chars + ']';
    }
}
