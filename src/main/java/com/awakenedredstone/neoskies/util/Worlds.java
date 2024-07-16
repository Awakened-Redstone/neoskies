package com.awakenedredstone.neoskies.util;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.logic.Hub;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.mixin.entity.EntityAccessor;
import com.awakenedredstone.neoskies.mixin.entity.ServerPlayerEntityAccessor;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;

import java.util.Optional;

public class Worlds {
    public static void returnToIslandSpawn(ServerPlayerEntity player, boolean fallDamage) {
        if (player.hasPassengers()) {
            player.removeAllPassengers();
        }

        player.stopRiding();

        if (NeoSkiesAPI.isIsland(player.getWorld())) {
            Optional<Island> islandOptional = NeoSkiesAPI.getOptionalIsland(player.getWorld());
            if (islandOptional.isPresent()) {
                Island island = islandOptional.get();
                if (island.isMember(player)) {
                    if (!fallDamage) player.fallDistance = 0;
                    FabricDimensions.teleport(player, island.getOverworld(), new TeleportTarget(island.spawnPos, new Vec3d(0, 0, 0), 0, 0));
                } else {
                    if (!fallDamage) player.fallDistance = 0;
                    FabricDimensions.teleport(player, island.getOverworld(), new TeleportTarget(island.visitsPos, new Vec3d(0, 0, 0), 0, 0));
                }
            }
        } else {
            Hub hub = IslandLogic.getInstance().hub;
            if (!IslandLogic.getConfig().safeVoidFallDamage) player.fallDistance = 0;
            hub.visit(player, true);
        }
    }

    public static RegistryKey<World> redirect(RegistryKey<World> registryKey) {
        if (NeoSkiesAPI.isOverworld(registryKey)) {
            return World.OVERWORLD;
        }
        if (NeoSkiesAPI.isEnd(registryKey)) {
            return World.END;
        }
        if (NeoSkiesAPI.isNether(registryKey)) {
            return World.NETHER;
        }
        return registryKey;
    }

    public static RegistryKey<World> redirect(World world) {
        return redirect(world.getRegistryKey());
    }

    public static void teleport(ServerPlayerEntity player, ServerWorld targetWorld, Vec3d pos, float yaw, float pitch) {
        teleport(player, targetWorld, pos.x, pos.y, pos.z, yaw, pitch);
    }

    public static void teleport(ServerPlayerEntity player, ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch) {
        player.setCameraEntity(player);
        player.stopRiding();
        if (targetWorld == player.getWorld()) {
            player.networkHandler.requestTeleport(x, y, z, yaw, pitch);
        } else {
            ServerWorld serverWorld = player.getServerWorld();
            WorldProperties worldProperties = targetWorld.getLevelProperties();
            if (!redirect(targetWorld).equals(redirect(player.getWorld()))) {
                player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(player.createCommonPlayerSpawnInfo(targetWorld), PlayerRespawnS2CPacket.KEEP_ALL));
            }
            player.networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
            player.server.getPlayerManager().sendCommandTree(player);
            serverWorld.removePlayer(player, Entity.RemovalReason.CHANGED_DIMENSION);
            ((EntityAccessor) player).callUnsetRemoved();
            player.refreshPositionAndAngles(x, y, z, yaw, pitch);
            player.setServerWorld(targetWorld);
            targetWorld.onPlayerTeleport(player);
            ((ServerPlayerEntityAccessor) player).callWorldChanged(serverWorld);
            player.networkHandler.requestTeleport(x, y, z, yaw, pitch);
            player.server.getPlayerManager().sendWorldInfo(player, targetWorld);
            player.server.getPlayerManager().sendPlayerStatus(player);
        }
    }
}
