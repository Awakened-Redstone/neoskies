package com.awakenedredstone.neoskies.util;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.logic.Hub;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.Optional;

public class Worlds {
    public static void returnToIslandSpawn(ServerPlayerEntity player, boolean fallDamage) {
        if (player.hasPassengers()) {
            player.removeAllPassengers();
        }

        player.stopRiding();

        if (NeoSkiesAPI.isIsland(player.getWorld())) {
            Optional<Island> islandOptional = NeoSkiesAPI.getIsland(player.getWorld());
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

}
