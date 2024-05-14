package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.SkylandsMain;
import com.awakenedredstone.neoskies.logic.Skylands;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class VisitCommand {

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("visit")
                .requires(Permissions.require("neoskies.island.visit", true))
                .then(argument("player", player())
                    .executes(context -> {
                        var visitor = context.getSource().getPlayer();
                        var owner = EntityArgumentType.getPlayer(context, "player");
                        if (visitor != null && owner != null) {
                            VisitCommand.run(visitor, owner);
                        }
                        return 1;
                    })
                )
            )
        );
    }

    static void run(ServerPlayerEntity visitor, ServerPlayerEntity owner) {
        String ownerName = owner.getName().getString();

        Skylands.getInstance().islands.getByPlayer(owner).ifPresentOrElse(island -> {
            if (!island.isMember(visitor) && island.isBanned(visitor)) {
                visitor.sendMessage(Texts.prefixed("message.neoskies.island_visit.ban", map -> map.put("owner", ownerName)));
            } else {
                if (!island.locked) {
                    if (visitor.getWorld().getRegistryKey().getValue().equals(SkylandsMain.id(island.owner.uuid.toString())) && !Skylands.getConfig().allowVisitCurrentIsland) {
                        visitor.sendMessage(Texts.prefixed("message.neoskies.island_visit.fail", map -> map.put("owner", ownerName)));
                    } else {
                        visitor.sendMessage(Texts.prefixed("message.neoskies.island_visit.success", map -> map.put("owner", ownerName)));
                        island.visitAsVisitor(visitor);
                    }
                } else {
                    visitor.sendMessage(Texts.prefixed("message.neoskies.island_visit.no_visits", map -> map.put("owner", ownerName)));
                }
            }

        }, () -> visitor.sendMessage(Texts.prefixed("message.neoskies.island_visit.no_island", map -> map.put("owner", ownerName))));
    }
}
