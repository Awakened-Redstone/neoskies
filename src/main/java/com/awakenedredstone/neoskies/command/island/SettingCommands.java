package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.gui.IslandSettingsGui;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.requiresIsland;
import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SettingCommands {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("settings")
                .requires(requiresIsland("neoskies.island.settings", true))
                .executes(context -> settingsGui(context.getSource()))
                .then(literal("lock")
                    .requires(requiresIsland("neoskies.island.lock", true))
                    .executes(context -> {
                        var player = context.getSource().getPlayer();
                        if (player != null) {
                            SettingCommands.toggleVisits(player);
                        }
                        return 1;
                    })
                ).then(literal("position")
                    .then(literal("spawn")
                        .requires(requiresIsland("neoskies.island.settings.position.spawn", true))
                        .then(argument("position", blockPos())
                            .executes(context -> {
                                var player = context.getSource().getPlayer();
                                var pos = BlockPosArgumentType.getBlockPos(context, "position");
                                if (player != null) {
                                    setSpawnPos(player, pos);
                                }
                                return 1;
                            })
                        )
                    ).then(literal("visit")
                        .requires(requiresIsland("neoskies.island.settings.position.visit", true))
                        .then(argument("position", blockPos())
                            .executes(context -> {
                                var player = context.getSource().getPlayer();
                                var pos = BlockPosArgumentType.getBlockPos(context, "position");
                                if (player != null) {
                                    setVisitsPos(player, pos);
                                }
                                return 1;
                            })
                        )
                    )
                )
            )
        );
    }

    static void toggleVisits(ServerPlayerEntity player) {
        IslandLogic.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            if (island.locked) {
                player.sendMessage(Texts.prefixed("message.neoskies.settings.unlock"));
                island.locked = false;
            } else {
                player.sendMessage(Texts.prefixed("message.neoskies.settings.lock"));
                island.locked = true;
            }
        }, () -> player.sendMessage(Texts.prefixed("message.neoskies.settings.no_island")));
    }

    static void setSpawnPos(ServerPlayerEntity player, BlockPos pos) {
        IslandLogic.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            island.spawnPos = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            String posText = pos.getX() + " " + pos.getY() + " " + pos.getZ();
            player.sendMessage(Texts.prefixed("message.neoskies.settings.spawn_pos_change", map -> map.put("pos", posText)));
        }, () -> player.sendMessage(Texts.prefixed("message.neoskies.settings.no_island")));
    }

    static void setVisitsPos(ServerPlayerEntity player, BlockPos pos) {
        IslandLogic.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            island.visitsPos = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            String posText = pos.getX() + " " + pos.getY() + " " + pos.getZ();
            player.sendMessage(Texts.prefixed("message.neoskies.settings.visits_pos_change", map -> map.put("pos", posText)));
        }, () -> player.sendMessage(Texts.prefixed("message.neoskies.settings.no_island")));
    }

    private static int settingsGui(ServerCommandSource source) {
        if (!source.isExecutedByPlayer()) {
            source.sendError(Texts.prefixed("message.neoskies.error.player_only"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();

        Optional<Island> optionalIsland = NeoSkiesAPI.getIslandByPlayer(player);
        optionalIsland.ifPresentOrElse(island -> {
            //noinspection DataFlowIssue
            player.playSoundToPlayer(SoundEvents.ENTITY_HORSE_SADDLE, SoundCategory.MASTER, 0.4f, 1);
            new IslandSettingsGui(island, null).openGui(player);
        }, () -> source.sendError(Texts.prefixed("message.neoskies.error.missing_island")));

        return 1;
    }
}
