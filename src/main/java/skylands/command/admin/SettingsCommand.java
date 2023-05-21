package skylands.command.admin;

import com.awakenedredstone.cbserverconfig.ui.ConfigScreen;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import skylands.SkylandsMain;
import skylands.util.Texts;

import static net.minecraft.server.command.CommandManager.literal;
import static skylands.command.utils.CommandUtils.adminNode;
import static skylands.command.utils.CommandUtils.registerAdmin;

public class SettingsCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        registerAdmin(dispatcher, adminNode()
            .then(literal("settings").requires(Permissions.require("skylands.admin.settings", 4))
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (!source.isExecutedByPlayer()) {
                        source.sendError(Texts.prefixed("message.skylands.error.player_only"));
                        return 0;
                    }

                    new ConfigScreen(source.getPlayer(), SkylandsMain.MAIN_CONFIG, null, null);

                    return 1;
                })
            )
        );
    }
}
