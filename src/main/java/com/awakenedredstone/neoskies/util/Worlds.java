package com.awakenedredstone.neoskies.util;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import com.awakenedredstone.neoskies.SkylandsMain;
import com.awakenedredstone.neoskies.api.SkylandsAPI;
import com.awakenedredstone.neoskies.logic.Hub;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.Skylands;

import java.util.Optional;

public class Worlds {
    public static void returnToIslandSpawn(ServerPlayerEntity player, boolean fallDamage) {
        if (player.hasPassengers()) {
            player.removeAllPassengers();
        }

        player.stopRiding();

        if (SkylandsAPI.isIsland(player.getWorld())) {
            Optional<Island> islandOptional = SkylandsAPI.getIsland(player.getWorld());
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
            Hub hub = Skylands.getInstance().hub;
            if (!Skylands.getConfig().safeVoidFallDamage) player.fallDistance = 0;
            hub.visit(player, true);
        }
    }

    public static RegistryKey<World> redirect(RegistryKey<World> registryKey) {
        if (SkylandsAPI.isOverworld(registryKey)) {
            return World.OVERWORLD;
        }
        if (SkylandsAPI.isEnd(registryKey)) {
            return World.END;
        }
        if (SkylandsAPI.isNether(registryKey)) {
            return World.NETHER;
        }
        return registryKey;
    }

}
