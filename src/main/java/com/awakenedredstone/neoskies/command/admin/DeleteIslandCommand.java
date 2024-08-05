package com.awakenedredstone.neoskies.command.admin;

import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.adminNode;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.registerAdmin;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DeleteIslandCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        registerAdmin(dispatcher, adminNode()
            .then(literal("delete-island").requires(Permissions.require("neoskies.admin.delete", 4))
                .then(argument("player", word())
                    .executes(context -> {
                        var playerName = StringArgumentType.getString(context, "player");
                        var island = IslandLogic.getInstance().islands.getByPlayer(playerName);

                        if (island.isPresent()) {
                            IslandLogic.getInstance().islands.delete(playerName);
                            context.getSource().sendFeedback(() -> Texts.translatable("message.neoskies.force_delete.success", map -> map.put("player", playerName)), true);
                        } else {
                            context.getSource().sendFeedback(() -> Texts.translatable("message.neoskies.force_delete.fail", map -> map.put("player", playerName)), true);
                        }

                        return 1;
                    })
                )
            )
        );
    }
}
