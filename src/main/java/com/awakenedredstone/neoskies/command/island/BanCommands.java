package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.logic.Member;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.requiresIsland;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BanCommands {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("ban")
                .requires(requiresIsland("neoskies.command.ban", true))
                .then(argument("player", player())
                    .executes(context -> {
                        var player = context.getSource().getPlayer();
                        var bannedPlayer = EntityArgumentType.getPlayer(context, "player");
                        if (player != null && bannedPlayer != null) {
                            BanCommands.ban(player, bannedPlayer);
                        }
                        return 1;
                    })
                )
            )
        );

        register(dispatcher, node().
            then(literal("unban")
                .requires(Permissions.require("neoskies.command.unban", true))
                .then(argument("player", word())
                    .suggests((context, builder) -> {
                        var player = context.getSource().getPlayer();

                        if (player != null) {
                            var island = IslandLogic.getInstance().islands.getByPlayer(player);
                            if (island.isPresent()) {
                                var bans = island.get().bans;

                                String remains = builder.getRemaining();

                                for (var member : bans) {
                                    if (member.name.contains(remains)) {
                                        builder.suggest(member.name);
                                    }
                                }
                                return builder.buildFuture();
                            }
                        }
                        return builder.buildFuture();
                    }).executes(context -> {
                        String unbanned = StringArgumentType.getString(context, "player");
                        var player = context.getSource().getPlayer();

                        if (player != null) {
                            BanCommands.unban(player, unbanned);
                        }
                        return 1;
                    })
                )
            )
        );
    }

    static void ban(ServerPlayerEntity player, ServerPlayerEntity banned) {
        IslandLogic.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            if (player.getName().getString().equals(banned.getName().getString())) {
                player.sendMessage(Texts.prefixed("message.neoskies.ban_player.yourself"));
            } else {
                if (island.isMember(banned)) {
                    player.sendMessage(Texts.prefixed("message.neoskies.ban_player.member"));
                } else {
                    if (island.isBanned(banned)) {
                        player.sendMessage(Texts.prefixed("message.neoskies.ban_player.fail"));
                    } else {
                        island.bans.add(new Member(banned));
                        player.sendMessage(Texts.prefixed("message.neoskies.ban_player.success", map -> map.put("player", banned.getName().getString())));
                        banned.sendMessage(Texts.prefixed("message.neoskies.ban_player.ban", map -> map.put("owner", island.owner.name)));

                        NeoSkiesAPI.getOptionalIsland(banned.getWorld()).ifPresent(isl -> {
                            if (isl.owner.uuid.equals(island.owner.uuid)) {
                                IslandLogic.getInstance().hub.visit(banned);
                            }
                        });
                    }
                }
            }
        }, () -> player.sendMessage(Texts.prefixed("message.neoskies.ban_player.no_island")));
    }

    static void unban(ServerPlayerEntity player, String unbanned) {
        IslandLogic.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            if (!island.isBanned(unbanned)) {
                player.sendMessage(Texts.prefixed("message.neoskies.unban_player.fail"));
            } else {
                island.bans.removeIf(member -> member.name.equals(unbanned));
                player.sendMessage(Texts.prefixed("message.neoskies.unban_player.success", map -> map.put("player", unbanned)));
            }
        }, () -> player.sendMessage(Texts.prefixed("message.neoskies.unban_player.no_island")));
    }
}
