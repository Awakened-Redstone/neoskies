package skylands.util;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import skylands.SkylandsMain;
import skylands.api.SkylandsAPI;
import skylands.logic.Hub;
import skylands.logic.Island;
import skylands.logic.Skylands;

import java.util.Optional;

public class Worlds {
    public static void teleportToIsland(ServerPlayerEntity player, boolean fallDamage) {
        if (player.hasPassengers()) {
            player.removeAllPassengers();
        }

        player.stopRiding();

        if (SkylandsAPI.isIsland(player.world)) {
            Optional<Island> islandOptional = SkylandsAPI.getIsland(player.world);
            if (islandOptional.isPresent()) {
                Island island = islandOptional.get();
                if (island.isMember(player)) {
                    if (!fallDamage) player.fallDistance = 0;
                    FabricDimensions.teleport(player, island.getWorld(), new TeleportTarget(island.spawnPos, new Vec3d(0, 0, 0), 0, 0));
                } else {
                    if (!fallDamage) player.fallDistance = 0;
                    FabricDimensions.teleport(player, island.getWorld(), new TeleportTarget(island.visitsPos, new Vec3d(0, 0, 0), 0, 0));
                }
            }
        } else if (SkylandsAPI.isHub(player.world)) {
            Hub hub = Skylands.getInstance().hub;
            if (!SkylandsMain.MAIN_CONFIG.getConfig().safeVoidFallDamage) player.fallDistance = 0;
            FabricDimensions.teleport(player, player.getWorld(), new TeleportTarget(hub.pos, new Vec3d(0, 0, 0), 0, 0));
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
