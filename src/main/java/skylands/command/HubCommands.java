package skylands.command;

import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import nota.player.SongPlayer;
import skylands.logic.Skylands;
import skylands.util.Texts;

import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static skylands.command.utils.CommandUtils.*;

public class HubCommands {

    static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node().then(literal("hub").requires(Permissions.require("skylands.teleport.hub", true))
                .executes(context -> {
                    var source = context.getSource();
                    var player = source.getPlayer();
                    MinecraftServer server = source.getServer();
                    if (player != null) {
                        HubCommands.visit(player, server);
                    }
                    return 1;
                })
        ));

        registerAdmin(dispatcher, adminNode()
                .then(literal("hub").requires(Permissions.require("skylands.admin.hub", 4))
                        .then(literal("pos").requires(Permissions.require("skylands.admin.hub.pos", 4))
                                .then(argument("position", blockPos()).executes(context -> {
                                    var pos = BlockPosArgumentType.getBlockPos(context, "position");
                                    var source = context.getSource();
                                    HubCommands.setPos(pos, source);
                                    return 1;
                                })))
                        .then(literal("protection").requires(Permissions.require("skylands.admin.hub.protection", 4))
                                .executes(context -> {
                                    HubCommands.toggleProtection(context.getSource());
                                    return 1;
                                })
                        ).then(literal("music").requires(Permissions.require("skylands.admin.hub.music", 4))
                                .executes(context -> {
                                    MinecraftServer server = context.getSource().getServer();
                                    Skylands skylands = Skylands.instance;
                                    SongPlayer sp = skylands.hub.songPlayer;
                                    if (sp != null) {
                                        sp.setPlaying(!sp.playing);
                                    } else {
                                        skylands.hub.initSongPlayer(server);
                                        server.getPlayerManager().getPlayerList().forEach(player -> skylands.hub.songPlayer.addPlayer(player));
                                        skylands.hub.songPlayer.setPlaying(true);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }

    static void visit(ServerPlayerEntity player, MinecraftServer server) {
        player.sendMessage(Texts.prefixed("message.skylands.hub_visit"));
        FabricDimensions.teleport(player, server.getOverworld(), new TeleportTarget(Skylands.instance.hub.pos, new Vec3d(0, 0, 0), 0, 0));
    }

    static void setPos(BlockPos pos, ServerCommandSource source) {
        Skylands.instance.hub.pos = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        String posText = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        source.sendFeedback(Texts.prefixed("message.skylands.hub_pos_change", map -> map.put("%pos%", posText)), true);
    }

    static void toggleProtection(ServerCommandSource source) {
        var hub = Skylands.instance.hub;
        if (hub.hasProtection) {
            hub.hasProtection = false;
            source.sendFeedback(Texts.prefixed("message.skylands.hub_protection.disable"), true);
        } else {
            hub.hasProtection = true;
            source.sendFeedback(Texts.prefixed("message.skylands.hub_protection.enable"), true);
        }
    }
}
