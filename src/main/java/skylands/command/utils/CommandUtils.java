package skylands.command.utils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import skylands.SkylandsMain;
import skylands.logic.Island;
import skylands.logic.Skylands;
import skylands.util.Texts;

import java.util.List;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class CommandUtils {

    public static SuggestionProvider<ServerCommandSource> ISLAND_SUGGESTIONS = (context, builder) -> {
        List<Island> islands = Skylands.getInstance().islands.stuck;
        for (Island island : islands) {
            builder.suggest(island.getIslandId().toString());
        }
        return builder.buildFuture();
    };

    public static boolean assertIsland(ServerCommandSource source, @Nullable Island island) {
        if (island == null) {
            source.sendError(Texts.of("message.skylands.error.island_not_found"));
            return false;
        }
        return true;
    }

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
