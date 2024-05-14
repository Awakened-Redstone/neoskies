package com.awakenedredstone.neoskies.command.utils;

import com.awakenedredstone.neoskies.api.SkylandsAPI;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.Skylands;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class CommandUtils {
    public static final SuggestionProvider<ServerCommandSource> ISLAND_SUGGESTIONS = (context, builder) -> {
        List<Island> islands = Skylands.getInstance().islands.stuck;
        for (Island island : islands) {
            builder.suggest(island.getIslandId().toString());
        }
        return builder.buildFuture();
    };

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean assertIsland(ServerCommandSource source, @Nullable Island island) {
        if (island == null) {
            source.sendError(Texts.of("message.neoskies.error.island_not_found"));
            return false;
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean assertPlayer(ServerCommandSource source) {
        if (!source.isExecutedByPlayer()) {
            source.sendError(Texts.of("message.neoskies.error.player_olny"));
            return false;
        }
        return true;
    }

    public static Predicate<ServerCommandSource> playerOnly(@NotNull String permission, boolean defaultValue) {
        return source -> Permissions.check(source, permission, defaultValue) && source.isExecutedByPlayer() ;
    }

    public static Predicate<ServerCommandSource> requiresIsland(@NotNull String permission, boolean defaultValue) {
        return source -> playerOnly(permission, defaultValue).test(source) && SkylandsAPI.hasIsland(source.getPlayer());
    }

    public static Predicate<ServerCommandSource> requiresNoIsland(@NotNull String permission, boolean defaultValue) {
        return source -> playerOnly(permission, defaultValue).test(source) && !SkylandsAPI.hasIsland(source.getPlayer());
    }

    public static Predicate<ServerCommandSource> mustBeIslandOwner(@NotNull String permission, boolean defaultValue) {
        return source -> requiresIsland(permission, defaultValue).test(source) && SkylandsAPI.isIslandOwner(source.getPlayer());
    }

    public static LiteralArgumentBuilder<ServerCommandSource> node() {
        return literal(Skylands.getConfig().command);
    }

    public static LiteralArgumentBuilder<ServerCommandSource> adminNode() {
        return literal(Skylands.getConfig().adminCommand);
    }

    public static LiteralCommandNode<ServerCommandSource> register(CommandDispatcher<ServerCommandSource> dispatcher, final LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(command);

        for (String alias : Skylands.getConfig().commandAliases) {
            dispatcher.register(CommandManager.literal(alias).redirect(node));
        }

        return node;
    }

    public static LiteralCommandNode<ServerCommandSource> registerAdmin(CommandDispatcher<ServerCommandSource> dispatcher, final LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(command);

        for (String alias : Skylands.getConfig().adminCommandAliases) {
            dispatcher.register(CommandManager.literal(alias).redirect(node));
        }

        return node;
    }
}
