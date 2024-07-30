package com.awakenedredstone.neoskies.util;

import com.awakenedredstone.neoskies.logic.IslandLogic;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.text.Text;

import java.util.Map;

public class DynamicPlaceholders {
    public static Text parseText(String inputText, Map<String, Text> placeholders) {
        return parseText(Text.literal(inputText), placeholders);
    }

    public static Text parseText(Text inputText, Map<String, Text> placeholders) {
        return Placeholders.parseText(Text.literal(Texts.getTextString(inputText)),
          PlaceholderContext.of(IslandLogic.getServer()),
          Placeholders.PLACEHOLDER_PATTERN_CUSTOM,
          id -> getPlaceholder(id, placeholders)
        );
    }

    private static PlaceholderHandler getPlaceholder(String id, Map<String, Text> placeholders) {
        return placeholders.containsKey(id) ? (ctx, arg) -> PlaceholderResult.value(placeholders.get(id)) : Placeholders.DEFAULT_PLACEHOLDER_GETTER.getPlaceholder(id);
    }
}
