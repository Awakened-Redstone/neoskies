package com.awakenedredstone.neoskies.command.admin;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.command.utils.CommandUtils;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.MapBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.adminNode;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.registerAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class IslandDataCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        registerAdmin(dispatcher, adminNode()
          .then(literal("island-data")
            .requires(Permissions.require("neoskies.admin.island.data", 4))
            .then(literal("find")
              .requires(Permissions.require("neoskies.admin.island.data.find", 4))
              .then(argument("player", StringArgumentType.word())
                .suggests((context, builder) -> {
                    List<Island> islands = IslandLogic.getInstance().islands.stuck;
                    for (Island island : islands) {
                        builder.suggest(island.owner.name);
                        island.members.forEach(member -> {
                            builder.suggest(member.name);
                        });
                    }
                    return builder.buildFuture();
                }).executes(context -> {
                    String playerName = StringArgumentType.getString(context, "player");
                    Optional<Island> islandOptional = NeoSkiesAPI.getIslandByPlayer(playerName);
                    return getIslandData(context.getSource(), islandOptional.orElse(null));
                })
              )
            ).then(literal("get")
              .requires(Permissions.require("neoskies.admin.island.data.get", 4))
              .then(argument("id", StringArgumentType.word())
                .suggests(CommandUtils.ISLAND_SUGGESTIONS)
                .executes(context -> {
                    String islandId = StringArgumentType.getString(context, "id");
                    Optional<Island> islandOptional = NeoSkiesAPI.getOptionalIsland(UUID.fromString(islandId));
                    return getIslandData(context.getSource(), islandOptional.orElse(null));
                })
              )
            )
          )
        );
    }

    private static int getIslandData(ServerCommandSource source, @Nullable Island island) {
        if (island == null) {
            source.sendError(Texts.translatable("message.neoskies.error.island_not_found"));
            return 0;
        }

        StringBuilder members = new StringBuilder();
        for (int i = 0; i < island.members.size(); i++) {
            members.append("    ");
            members.append(island.members.get(i).name);
            if (i != island.members.size() - 1) {
                members.append(",\n");
            }
        }

        MapBuilder.StringMap map = new MapBuilder.StringMap()
          .put("id", island.getIslandId().toString())
          .put("owner", island.owner.name)
          .put("members", members.toString())
          .putAny("balance", island.getWallet().balance())
          .putAny("has_nether", island.hasNether)
          .putAny("has_end", island.hasEnd)
          .putAny("spawn_pos", island.spawnPos)
          .putAny("visit_pos", island.visitsPos)
          .putAny("radius", island.radius)
          .putAny("locked", island.locked)
          .putAny("created", island.getCreated().toEpochMilli());
        source.sendFeedback(() -> Texts.translatable("message.neoskies.island_data", map.build()), false);
        return 1;
    }
}
