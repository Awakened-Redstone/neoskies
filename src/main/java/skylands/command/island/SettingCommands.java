package skylands.command.island;

import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import skylands.api.SkylandsAPI;
import skylands.gui.IslandSettingsGui;
import skylands.logic.Island;
import skylands.logic.Skylands;
import skylands.util.Texts;

import java.util.Optional;

import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static skylands.command.utils.CommandUtils.node;
import static skylands.command.utils.CommandUtils.register;

public class SettingCommands {

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("settings")
                .requires(Permissions.require("skylands.island.settings", true))
                .executes(context -> settingsGui(context.getSource()))
                .then(literal("lock")
                    .requires(Permissions.require("skylands.island.lock", true))
                    .executes(context -> {
                        var player = context.getSource().getPlayer();
                        if (player != null) {
                            SettingCommands.toggleVisits(player);
                        }
                        return 1;
                    })
                ).then(literal("position")
                    .then(literal("spawn")
                        .requires(Permissions.require("skylands.island.settings.position.spawn", true))
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
                        .requires(Permissions.require("skylands.island.settings.position.visit", true))
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
        Skylands.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            if (island.locked) {
                player.sendMessage(Texts.prefixed("message.skylands.settings.unlock"));
                island.locked = false;
            } else {
                player.sendMessage(Texts.prefixed("message.skylands.settings.lock"));
                island.locked = true;
            }

        }, () -> player.sendMessage(Texts.prefixed("message.skylands.settings.no_island")));
    }

    static void setSpawnPos(ServerPlayerEntity player, BlockPos pos) {
        Skylands.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            island.spawnPos = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            String posText = pos.getX() + " " + pos.getY() + " " + pos.getZ();
            player.sendMessage(Texts.prefixed("message.skylands.settings.spawn_pos_change", map -> map.put("pos", posText)));

        }, () -> player.sendMessage(Texts.prefixed("message.skylands.settings.no_island")));
    }

    static void setVisitsPos(ServerPlayerEntity player, BlockPos pos) {
        Skylands.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            island.visitsPos = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            String posText = pos.getX() + " " + pos.getY() + " " + pos.getZ();
            player.sendMessage(Texts.prefixed("message.skylands.settings.visits_pos_change", map -> map.put("pos", posText)));

        }, () -> player.sendMessage(Texts.prefixed("message.skylands.settings.no_island")));
    }

    private static int settingsGui(ServerCommandSource source) {
        if (!source.isExecutedByPlayer()) {
            source.sendError(Texts.prefixed("message.skylands.error.player_only"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();

        Optional<Island> optionalIsland = SkylandsAPI.getIslandByPlayer(player);
        optionalIsland.ifPresentOrElse(island -> {
            //noinspection DataFlowIssue
            player.playSound(SoundEvents.ENTITY_HORSE_SADDLE, SoundCategory.MASTER, 0.4f, 1);
            new IslandSettingsGui(island, null).openGui(player);
        }, () -> source.sendError(Texts.prefixed("message.skylands.error.missing_island")));

        return 1;
    }
}
