package com.awakenedredstone.neoskies.command.island;

import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.placeholders.api.TextParserUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Language;

import static net.minecraft.server.command.CommandManager.literal;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;

public class HelpCommand {

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("help")
                .requires(Permissions.require("neoskies.command.help", true))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        HelpCommand.run(player);
                    }
                    return 1;
                })
            )
        );
    }

    static void run(ServerPlayerEntity player) {
        Language lang = Language.getInstance();
        StringBuilder text = new StringBuilder();
        String key = "message.neoskies.help.";

        for (int i = 0; i <= 32; i++) {
            if (lang.hasTranslation(key + i)) {
                text.append(lang.get(key + i));
            }
            if (lang.hasTranslation(key + (i + 1))) {
                text.append("\n");
            } else {
                break;
            }
        }
        player.sendMessage(TextParserUtils.formatText(text.toString()));
    }
}