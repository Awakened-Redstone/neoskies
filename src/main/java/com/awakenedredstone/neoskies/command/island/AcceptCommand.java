package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Players;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.requiresIsland;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AcceptCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("accept")
                .requires(requiresIsland("neoskies.command.accept", true))
                .then(argument("player", word())
                    .executes(context -> {
                        String inviter = StringArgumentType.getString(context, "player");
                        var player = context.getSource().getPlayer();

                        if (player != null) {
                            AcceptCommand.run(player, inviter);
                        }
                        return 1;
                    })
                )
            )
        );
    }

    static void run(ServerPlayerEntity player, String ownerName) {
        var inviter = Players.get(ownerName);
        if (inviter.isEmpty()) {
            player.sendMessage(Texts.prefixed("message.neoskies.accept.no_player"));
            return;
        }

        var island = IslandLogic.getInstance().islands.getByPlayer(inviter.get());
        if (island.isEmpty()) {
            player.sendMessage(Texts.prefixed("message.neoskies.accept.no_island"));
            return;
        }

        var invite = IslandLogic.getInstance().invites.get(island.get(), player);
        if (invite.isEmpty()) {
            player.sendMessage(Texts.prefixed("message.neoskies.accept.fail"));
            return;
        }

        if (!invite.get().accepted) {
            invite.get().accept(player);
            player.sendMessage(Texts.prefixed("message.neoskies.accept.success", map -> map.put("owner", ownerName)));
        }
    }
}
