package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.requiresIsland;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class KickCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("kick")
                .requires(requiresIsland("neoskies.command.kick", true))
                .then(argument("player", player())
                    .executes(context -> {
                        var player = context.getSource().getPlayer();
                        var kickedPlayer = EntityArgumentType.getPlayer(context, "player");
                        if (player != null && kickedPlayer != null) {
                            KickCommand.run(player, kickedPlayer);
                        }
                        return 1;
                    })
                )
            )
        );
    }

    static void run(ServerPlayerEntity player, ServerPlayerEntity kicked) {
        IslandLogic.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            if (player.getName().getString().equals(kicked.getName().getString())) {
                player.sendMessage(Texts.prefixed("message.neoskies.kick_visitor.yourself"));
            } else {
                if (island.isMember(kicked)) {
                    player.sendMessage(Texts.prefixed("message.neoskies.kick_visitor.member"));
                } else {
                    NeoSkiesAPI.getOptionalIsland(kicked.getWorld()).ifPresent(isl -> {
                        if (isl.owner.uuid.equals(island.owner.uuid)) {
                            player.sendMessage(Texts.prefixed("message.neoskies.kick_visitor.success", map -> map.put("player", kicked.getName().getString())));

                            kicked.sendMessage(Texts.prefixed("message.neoskies.kick_visitor.kick", map -> map.put("owner", player.getName().getString())));
                            IslandLogic.getInstance().hub.visit(kicked);
                        } else {
                            player.sendMessage(Texts.prefixed("message.neoskies.kick_visitor.fail", map -> map.put("player", kicked.getName().getString())));
                        }
                    });
                }
            }
        }, () -> player.sendMessage(Texts.prefixed("message.neoskies.kick_visitor.no_island")));
    }
}
