package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.adminNode;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.registerAdmin;
import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HubCommands {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
          .then(literal("hub")
            .requires(Permissions.require("neoskies.teleport.hub", true))
            .executes(context -> {
                var source = context.getSource();
                var player = source.getPlayer();
                MinecraftServer server = source.getServer();
                if (player != null) {
                    HubCommands.visit(player, server);
                }
                return 1;
            })
          )
        );

        registerAdmin(dispatcher, adminNode()
          .then(literal("hub").requires(Permissions.require("neoskies.admin.hub", 4))
            .then(literal("pos").requires(Permissions.require("neoskies.admin.hub.pos", 4))
              .then(argument("position", blockPos()).executes(context -> {
                  var pos = BlockPosArgumentType.getBlockPos(context, "position");
                  var source = context.getSource();
                  HubCommands.setPos(pos, source);
                  return 1;
              }))
            ).then(literal("protection").requires(Permissions.require("neoskies.admin.hub.protection", 4))
              .executes(context -> {
                  HubCommands.toggleProtection(context.getSource());
                  return 1;
              })
            )/*.then(literal("settings").requires(Permissions.require("neoskies.admin.hub.settings", 4))
              .executes(context -> { })
            )*/
          )
        );
    }

    static void visit(ServerPlayerEntity player, MinecraftServer server) {
        IslandLogic.getInstance().hub.visit(player);
    }

    static void setPos(BlockPos pos, ServerCommandSource source) {
        IslandLogic.getInstance().hub.pos = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        String posText = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        source.sendFeedback(() -> Texts.prefixed("message.neoskies.hub_pos_change", map -> map.put("pos", posText)), true);
    }

    static void toggleProtection(ServerCommandSource source) {
        var hub = IslandLogic.getInstance().hub;
        if (hub.hasProtection) {
            hub.hasProtection = false;
            source.sendFeedback(() -> Texts.prefixed("message.neoskies.hub_protection.disable"), true);
        } else {
            hub.hasProtection = true;
            source.sendFeedback(() -> Texts.prefixed("message.neoskies.hub_protection.enable"), true);
        }
    }
}
