package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.logic.IslandStuck;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.mustBeIslandOwner;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DeleteCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("delete")
                .requires(mustBeIslandOwner("neoskies.island.delete", true))
                .executes(context -> {
                    var player = context.getSource().getPlayer();
                    if (player != null) DeleteCommand.warn(player);
                    return 1;
                }).then(argument("confirmation", word()).executes(context -> {
                    var player = context.getSource().getPlayer();
                    String confirmWord = StringArgumentType.getString(context, "confirmation");
                    if (player != null) DeleteCommand.run(player, confirmWord);
                    return 1;
                }))
            )
        );
    }

    static void run(ServerPlayerEntity player, String confirmWord) {
        if (confirmWord.equals("CONFIRM")) {
            IslandStuck islands = IslandLogic.getInstance().islands;

            islands.getByPlayer(player).ifPresentOrElse(island -> {
                var created = island.getCreated();
                var now = Instant.now();
                var seconds = ChronoUnit.SECONDS.between(created, now);

                if (seconds >= IslandLogic.getConfig().deletionCooldown) {
                    islands.delete(player);
                    player.sendMessage(Texts.prefixed("message.neoskies.island_delete.success"));
                } else {
                    player.sendMessage(Texts.prefixed("message.neoskies.island_delete.too_often"));
                }
            }, () -> {
                player.sendMessage(Texts.prefixed("message.neoskies.island_delete.fail"));
            });
        } else {
            player.sendMessage(Texts.prefixed("message.neoskies.island_delete.warning"));
        }
    }

    static void warn(ServerPlayerEntity player) {
        IslandStuck islands = IslandLogic.getInstance().islands;

        islands.getByPlayer(player).ifPresentOrElse(island -> {
            var created = island.getCreated();
            var now = Instant.now();
            var seconds = ChronoUnit.SECONDS.between(created, now);

            if (seconds >= IslandLogic.getConfig().deletionCooldown) {
                player.sendMessage(Texts.prefixed("message.neoskies.island_delete.warning"));
            } else {
                player.sendMessage(Texts.prefixed("message.neoskies.island_delete.too_often"));
            }
        }, () -> {
            player.sendMessage(Texts.prefixed("message.neoskies.island_delete.fail"));
        });
    }
}
