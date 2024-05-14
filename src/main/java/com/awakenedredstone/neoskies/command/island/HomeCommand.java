package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.SkylandsMain;
import com.awakenedredstone.neoskies.api.SkylandsAPI;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.Skylands;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.*;
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
        Skylands.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            Optional<Island> currentIsland = SkylandsAPI.getIsland(player.getWorld());
            boolean isHome = currentIsland.isPresent() && currentIsland.get().equals(island);
            if (isHome && !Skylands.getConfig().allowVisitCurrentIsland) {
                player.sendMessage(Texts.prefixed("message.neoskies.home.fail"));
            } else {
                player.sendMessage(Texts.prefixed("message.neoskies.home.success"));
                island.visitAsMember(player);
            }
        }, () -> player.sendMessage(Texts.prefixed("message.neoskies.home.no_island")));
    }

    static void run(ServerPlayerEntity visitor, String islandOwner) {
        Skylands.getInstance().islands.getByPlayer(islandOwner).ifPresentOrElse(island -> {
            if (visitor.getWorld().getRegistryKey().getValue().equals(SkylandsMain.id(island.owner.uuid.toString())) && !Skylands.getConfig().allowVisitCurrentIsland) {
                visitor.sendMessage(Texts.prefixed("message.neoskies.visit_home.fail", map -> map.put("owner", islandOwner)));
            } else {
                if (island.isMember(visitor)) {
                    visitor.sendMessage(Texts.prefixed("message.neoskies.visit_home.success", map -> map.put("owner", islandOwner)));
                    island.visitAsMember(visitor);
                } else {
                    visitor.sendMessage(Texts.prefixed("message.neoskies.visit_home.not_member"));
                }
            }
        }, () -> {
            visitor.sendMessage(Texts.prefixed("message.neoskies.visit_home.no_island"));
        });
    }
}
