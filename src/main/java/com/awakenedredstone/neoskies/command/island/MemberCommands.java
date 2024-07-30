package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.requiresIsland;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MemberCommands {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node().then(literal("members")
            .then(literal("invite").requires(requiresIsland("neoskies.command.members.invite", true))
                .then(argument("player", player())
                    .executes(context -> {
                        var player = context.getSource().getPlayer();
                        var newcomer = EntityArgumentType.getPlayer(context, "player");
                        if (player != null && newcomer != null) {
                            MemberCommands.invite(player, newcomer);
                        }
                        return 1;
                    })
                )
            ).then(literal("remove").requires(requiresIsland("neoskies.command.members.remove", true))
                .then(argument("player", word())
                    .suggests((context, builder) -> {
                        var player = context.getSource().getPlayer();

                        if (player != null) {
                            var island = IslandLogic.getInstance().islands.getByPlayer(player);
                            if (island.isPresent()) {
                                var members = island.get().members;

                                String remains = builder.getRemaining();

                                for (var member : members) {
                                    if (member.name.contains(remains)) {
                                        builder.suggest(member.name);
                                    }
                                }
                                return builder.buildFuture();
                            }
                        }
                        return builder.buildFuture();
                    }).executes(context -> {
                        String memberToRemove = StringArgumentType.getString(context, "player");
                        var player = context.getSource().getPlayer();
                        if (player != null) {
                            MemberCommands.remove(player, memberToRemove);
                        }
                        return 1;
                    })
                )
            )
        ));
    }

    static void invite(ServerPlayerEntity inviter, ServerPlayerEntity newcomer) {
        if (NeoSkiesAPI.getIslandByPlayer(newcomer).isPresent()) {
            inviter.sendMessage(Texts.prefixed("message.neoskies.invite_member.already_has_island"));
            return;
        }

        IslandLogic.getInstance().islands.getByPlayer(inviter).ifPresentOrElse(island -> {
            if (island.isMember(newcomer)) {
                inviter.sendMessage(Texts.prefixed("message.neoskies.invite_member.already_member"));
                return;
            }

            if (IslandLogic.getInstance().invites.hasInvite(island, newcomer)) {
                inviter.sendMessage(Texts.prefixed("message.neoskies.invite_member.already_invited"));
            } else {
                inviter.sendMessage(Texts.prefixed("message.neoskies.invite_member.success", (map) -> map.put("newcomer", newcomer.getName().getString())));

                var hoverText = Texts.prefixed("hover_event.neoskies.invite_member.accept", (map) -> map.put("inviter", inviter.getName().getString()));
                Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sl accept " + inviter.getName().getString()));
                style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

                var inviteText = Texts.prefixed("message.neoskies.invite_member.invite", (map) -> map.put("inviter", inviter.getName().getString()));

                newcomer.sendMessage(inviteText.getWithStyle(style).get(0));
                newcomer.sendMessage(Texts.prefixed("message.neoskies.invite_member.accept").getWithStyle(style).get(0));
                IslandLogic.getInstance().invites.create(island, newcomer);
            }
        }, () -> inviter.sendMessage(Texts.prefixed("message.neoskies.invite_member.no_island")));
    }

    static void remove(ServerPlayerEntity player, String removed) {
        IslandLogic.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            if (player.getName().getString().equals(removed)) {
                player.sendMessage(Texts.prefixed("message.neoskies.remove_member.yourself"));
            } else {
                if (island.isMember(removed)) {
                    island.members.removeIf(member -> member.name.equals(removed));
                    player.sendMessage(Texts.prefixed("message.neoskies.remove_member.success", (map) -> map.put("member", removed)));
                } else {
                    player.sendMessage(Texts.prefixed("message.neoskies.remove_member.not_member"));
                }
            }
        }, () -> player.sendMessage(Texts.prefixed("message.neoskies.remove_member.no_island")));
    }
}
