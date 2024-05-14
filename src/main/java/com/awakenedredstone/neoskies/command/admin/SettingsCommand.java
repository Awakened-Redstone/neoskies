package com.awakenedredstone.neoskies.command.admin;

import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.adminNode;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.registerAdmin;
import static net.minecraft.server.command.CommandManager.literal;

public class SettingsCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        registerAdmin(dispatcher, adminNode()
            .then(literal("settings").requires(Permissions.require("neoskies.admin.settings", 4))
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (!source.isExecutedByPlayer()) {
                        source.sendError(Texts.prefixed("message.neoskies.error.player_only"));
                        return 0;
                    }

                    //new ConfigScreen(source.getPlayer(), NeoSkies.MAIN_CONFIG, null, null);
                    source.sendFeedback(() -> Text.literal("In development, they will likely require the client mod"), false);

                    return 1;
                })
            )
        );
    }
}
