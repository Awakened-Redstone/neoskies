package skylands.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import skylands.SkylandsMain;

import java.util.Map;
import java.util.function.Consumer;

public class ServerUtils {

    public static void protectionWarning(PlayerEntity player, String key) {
        if (SkylandsMain.MAIN_CONFIG.showProtectionMessages()) actionbarPrefixed(player, "message.skylands.world_protection." + key);
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
