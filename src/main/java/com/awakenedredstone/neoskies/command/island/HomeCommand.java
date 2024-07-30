package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.requiresIsland;
import static net.minecraft.server.command.CommandManager.literal;

public class HomeCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("home").requires(requiresIsland("neoskies.teleport.home", true))
                .executes(context -> {
                    var player = context.getSource().getPlayer();
                    if (player != null) {
                        HomeCommand.run(player);
                    }
                    return 1;
                })
            )
        );
    }

    static void run(ServerPlayerEntity player) {
        IslandLogic.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            Optional<Island> currentIsland = NeoSkiesAPI.getOptionalIsland(player.getWorld());
            boolean isHome = currentIsland.isPresent() && currentIsland.get().equals(island);
            if (isHome && !IslandLogic.getConfig().allowVisitCurrentIsland) {
                player.sendMessage(Texts.prefixed("message.neoskies.home.fail"));
            } else {
                player.sendMessage(Texts.prefixed("message.neoskies.home.success"));
                island.visitAsMember(player);
            }
        }, () -> player.sendMessage(Texts.prefixed("message.neoskies.home.no_island")));
    }
}
