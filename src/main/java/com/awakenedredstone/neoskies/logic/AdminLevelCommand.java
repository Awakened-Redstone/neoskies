package com.awakenedredstone.neoskies.logic;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.command.utils.CommandUtils;
import com.awakenedredstone.neoskies.util.MapBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.UnitConvertions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.adminNode;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.registerAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class AdminLevelCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        registerAdmin(dispatcher, adminNode()
          .then(literal("level").requires(Permissions.require("neoskies.admin.level", 4))
            .then(literal("scan").requires(Permissions.require("neoskies.admin.level.scan", 4))
              .then(argument("island", StringArgumentType.word())
                .suggests(CommandUtils.ISLAND_SUGGESTIONS)
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    String islandId = StringArgumentType.getString(context, "island");
                    Island island = NeoSkiesAPI.getOptionalIsland(UUID.fromString(islandId)).orElse(null);

                    if (island == null) {
                        source.sendError(Texts.translatable("commands.neoskies.error.missing_island"));
                        return 0;
                    }

                    if (island.isScanning()) {
                        source.sendError(Texts.translatable("commands.neoskies.level.scan.error.busy"));
                        return 0;
                    }

                    source.sendFeedback(() -> Texts.translatable("commands.neoskies.level.scan.queued"), false);

                    AtomicInteger total = new AtomicInteger();
                    IslandLogic.getInstance().islandScanner.queueScan(island, integer -> {
                        source.sendMessage(Texts.translatable("commands.neoskies.level.scan.total", new MapBuilder.StringMap().putAny("total", integer).build()));
                        total.set(integer);
                    }, integer -> {
                        source.sendMessage(Texts.translatable("commands.neoskies.level.scan.progress", new MapBuilder.StringMap()
                          .putAny("total", total.get())
                          .putAny("current", integer)
                          .build()));
                    }, (timeTaken, scannedBlocks) -> {
                        source.sendMessage(Texts.translatable("commands.neoskies.level.scan.finished", new MapBuilder.StringMap()
                          .putAny("total", UnitConvertions.readableNumber(scannedBlocks.values().stream().mapToInt(value -> value).sum()))
                          .putAny("time", UnitConvertions.formatTimings(timeTaken))
                          .build()));
                    }, () -> {
                        source.sendError(Texts.translatable("commands.neoskies.level.scan.error"));
                    });

                    return 1;
                })
              )
            ).then(literal("set")
              .then(argument("island", StringArgumentType.word())
                .suggests(CommandUtils.ISLAND_SUGGESTIONS)
                .then(argument("amount", IntegerArgumentType.integer(0))
                  .then(literal("points")
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        String islandId = StringArgumentType.getString(context, "island");
                        int points = IntegerArgumentType.getInteger(context, "amount");
                        Island island = NeoSkiesAPI.getOptionalIsland(UUID.fromString(islandId)).orElse(null);

                        if (island == null) {
                            source.sendError(Texts.translatable("commands.neoskies.error.missing_island"));
                            return 0;
                        }

                        island.setPoints(points);
                        return 0;
                    })
                  )
                ).then(argument("amount", IntegerArgumentType.integer(0))
                  .then(literal("levels")
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        String islandId = StringArgumentType.getString(context, "island");
                        int points = IntegerArgumentType.getInteger(context, "amount");
                        Island island = NeoSkiesAPI.getOptionalIsland(UUID.fromString(islandId)).orElse(null);

                        if (island == null) {
                            source.sendError(Texts.translatable("commands.neoskies.error.missing_island"));
                            return 0;
                        }

                        island.setLevel(points);
                        return 0;
                    })
                  )
                )
              )
            )
          )
        );
    }
}
