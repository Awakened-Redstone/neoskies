package com.awakenedredstone.neoskies.util;

import com.awakenedredstone.neoskies.logic.IslandLogic;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.ParserBuilder;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import xyz.nucleoid.server.translations.api.Localization;
import xyz.nucleoid.server.translations.api.language.ServerLanguage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Texts {
    private static final Map<String, String> EMPTY_STRING_MAP = Collections.emptyMap();
    private static final Map<String, Text> EMPTY_TEXT_MAP = Collections.emptyMap();
    public static final String PREFIX = "neoskies.prefix";
    private static final NodeParser PARSER = ParserBuilder.of()
      .globalPlaceholders()
      .simplifiedTextFormat()
      .quickText()
      .build();

    public static String getTextString(Text text) {
        if (text.getContent() instanceof TranslatableTextContent tanslatable) {
            return Localization.text(text, ServerLanguage.getLanguage(IslandLogic.getConfig().language)).getString();
        } else {
            return text.getString();
        }
    }

    public static MutableText prefixed(String prefixKey, Text key, Map<String, Text> placeholders) {
        Text prefix = of(Text.translatable(prefixKey));
        Text text = of(key, placeholders);

        return Text.empty().append(prefix).append(text);
    }

    public static MutableText prefixed(String prefixKey, Text key, Consumer<Map<String, Text>> builder) {
        Map<String, Text> placeholders = new HashMap<>();
        builder.accept(placeholders);

        return prefixed(prefixKey, key, placeholders);
    }

    public static MutableText prefixed(String prefixKey, Text key) {
        return prefixed(prefixKey, key, EMPTY_TEXT_MAP);
    }

    public static MutableText prefixed(Text key, Consumer<Map<String, Text>> builder) {
        return prefixed(PREFIX, key, builder);
    }

    public static MutableText prefixed(Text key, Map<String, Text> placeholders) {
        return prefixed(PREFIX, key, placeholders);
    }

    public static MutableText prefixed(Text key) {
        return prefixed(key, EMPTY_TEXT_MAP);
    }

    public static MutableText prefixed(String prefixKey, String key, Map<String, String> placeholders) {
        Text prefix = of(Text.translatable(prefixKey));
        Text text = translatable(key, placeholders);

        return Text.empty().append(prefix).append(text);
    }

    public static MutableText prefixed(String prefixKey, String key, Consumer<Map<String, String>> builder) {
        Map<String, String> placeholders = new HashMap<>();
        builder.accept(placeholders);

        return prefixed(prefixKey, key, placeholders);
    }

    public static MutableText prefixed(String prefixKey, String key) {
        return prefixed(prefixKey, key, EMPTY_STRING_MAP);
    }

    public static MutableText prefixed(String key, Consumer<Map<String, String>> builder) {
        return prefixed(PREFIX, key, builder);
    }

    public static MutableText prefixed(String key, Map<String, String> placeholders) {
        return prefixed(PREFIX, key, placeholders);
    }

    public static MutableText prefixed(String key) {
        return prefixed(key, EMPTY_STRING_MAP);
    }

    public static MutableText literal(String key) {
        return of(Text.literal(key));
    }

    public static MutableText translatable(String key) {
        return of(Text.translatable(key));
    }

    public static MutableText translatable(String key, Map<String, String> placeholders1) {
        Map<String, Text> placeholders = new HashMap<>();
        placeholders1.forEach((k, v) -> placeholders.put(k, Text.literal(v)));

        return of(Text.translatable(key), placeholders);
    }

    public static MutableText translatable(String key, Consumer<Map<String, String>> builder) {
        Map<String, String> placeholders1 = new HashMap<>();
        builder.accept(placeholders1);

        Map<String, Text> placeholders = new HashMap<>();
        placeholders1.forEach((k, v) -> placeholders.put(k, Text.literal(v)));

        return of(Text.translatable(key), placeholders);
    }

    public static MutableText of(Text key, Map<String, Text> placeholders) {
        Text text = DynamicPlaceholders.parseText(key, placeholders);
        return (MutableText) PARSER.parseText(text.getString(), ParserContext.of());
    }

    public static MutableText of(Text key, Consumer<Map<String, Text>> builder) {
        Map<String, Text> placeholders = new HashMap<>();
        builder.accept(placeholders);

        return of(key, placeholders);
    }

    public static MutableText of(Text key) {
        return of(key, EMPTY_TEXT_MAP);
    }

    public static MutableText loreBase() {
        return Text.empty().setStyle(Style.EMPTY.withItalic(false));
    }

    public static MutableText loreBase(Text text) {
        return loreBase().append(text);
    }

    public static MutableText loreBase(String text) {
        return loreBase(Texts.translatable(text));
    }

    public static MutableText loreBase(String text, Consumer<Map<String, String>> builder) {
        return loreBase(Texts.translatable(text, builder));
    }

    public static MutableText loreBase(Text text, Consumer<Map<String, Text>> builder) {
        return loreBase(Texts.of(text, builder));
    }
}
