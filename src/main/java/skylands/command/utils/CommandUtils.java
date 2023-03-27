package skylands.command.utils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import skylands.SkylandsMain;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class CommandUtils {

    public static LiteralArgumentBuilder<ServerCommandSource> node() {
        return literal(SkylandsMain.MAIN_CONFIG.command());
    }

    public static LiteralArgumentBuilder<ServerCommandSource> adminNode() {
        return literal(SkylandsMain.MAIN_CONFIG.adminCommand());
    }

    public static LiteralCommandNode<ServerCommandSource> register(CommandDispatcher<ServerCommandSource> dispatcher, final LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(command);

        for (String alias : SkylandsMain.MAIN_CONFIG.commandAliases()) {
            dispatcher.register(CommandManager.literal(alias).redirect(node));
        }

        return node;
    }

    public static LiteralCommandNode<ServerCommandSource> registerAdmin(CommandDispatcher<ServerCommandSource> dispatcher, final LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(command);

        for (String alias : SkylandsMain.MAIN_CONFIG.adminCommandAliases()) {
            dispatcher.register(CommandManager.literal(alias).redirect(node));
        }

        return node;
    }
}
