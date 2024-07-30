package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandStuck;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.requiresNoIsland;
import static net.minecraft.server.command.CommandManager.literal;

public class CreateCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("create")
                .requires(requiresNoIsland("neoskies.island.create", true))
                .executes(context -> {
                    var source = context.getSource();
                    var player = source.getPlayer();
                    if (player != null) {
                        CreateCommand.run(player);
                    }
                    return 1;
                })
            )
        );
    }

    static void run(ServerPlayerEntity player) {
        IslandStuck islands = IslandLogic.getInstance().islands;

        if (islands.getByPlayer(player).isPresent()) {
            player.sendMessage(Texts.prefixed("message.neoskies.island_create.fail"));
        } else {
            Island island = islands.create(player);
            island.onFirstLoad(player);
            player.sendMessage(Texts.prefixed("message.neoskies.island_create.success"));
        }
    }
}
