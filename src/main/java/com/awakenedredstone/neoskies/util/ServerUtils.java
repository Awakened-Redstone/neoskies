package com.awakenedredstone.neoskies.util;

import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.Consumer;

public class ServerUtils {
    public static void protectionWarning(PlayerEntity player, String key) {
        if (IslandLogic.getConfig().showProtectionMessages) {
            actionbarPrefixed(player, "island_protection." + key);
        }
    }

    public static void protectionWarning(PlayerEntity player, IslandSettings settings) {
        if (IslandLogic.getConfig().showProtectionMessages) {
            actionbarPrefixed(player, "island_protection." + settings.getTranslationKey());
        }
    }

    public static void actionbarPrefixed(PlayerEntity player, String message, Consumer<Map<String, String>> builder) {
        actionbar(player, Texts.prefixed(message, builder));
    }

    public static void actionbarPrefixed(PlayerEntity player, String message) {
        actionbar(player, Texts.prefixed(message));
    }

    public static void actionbar(PlayerEntity player, Text message) {
        player.sendMessage(message, true);
    }
}
