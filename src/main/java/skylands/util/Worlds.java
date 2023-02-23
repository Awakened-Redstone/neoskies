package skylands.util;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import skylands.SkylandsMain;
import skylands.logic.Hub;
import skylands.logic.Island;
import skylands.logic.Skylands;

import java.util.Optional;
import java.util.UUID;

public class Worlds {

    public static boolean isHub(World world) {
        return world.getRegistryKey() == World.OVERWORLD;
    }

    public static boolean isIsland(World world) {
        return isIsland(world.getRegistryKey());
    }

    public static boolean isIsland(RegistryKey<World> registryKey) {
        var namespace = registryKey.getValue().getNamespace();
        return namespace.equals(Constants.NAMESPACE) || namespace.equals(Constants.NAMESPACE_NETHER) || namespace.equals(Constants.NAMESPACE_END);
    }

    public static boolean isOverworld(RegistryKey<World> registryKey) {
        var namespace = registryKey.getValue().getNamespace();
        return namespace.equals(SkylandsMain.MOD_ID) || registryKey == World.OVERWORLD;
    }

    public static boolean isNether(RegistryKey<World> registryKey) {
        var namespace = registryKey.getValue().getNamespace();
        return namespace.equals(Constants.NAMESPACE_NETHER) || registryKey == World.NETHER;
    }

    public static boolean isEnd(RegistryKey<World> registryKey) {
        var namespace = registryKey.getValue().getNamespace();
        return namespace.equals(Constants.NAMESPACE_END) || registryKey == World.END;
    }

    public static void teleportToIsland(ServerPlayerEntity player, boolean fallDamage) {
        if (player.hasPassengers()) {
            player.removeAllPassengers();
        }

        player.stopRiding();

        if (Worlds.isIsland(player.world)) {
            Optional<Island> islandOptional = Worlds.getIsland(player.world);
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
        } else if (Worlds.isHub(player.world)) {
            Hub hub = Skylands.getInstance().hub;
            if (!SkylandsMain.MAIN_CONFIG.getConfig().safeVoidFallDamage) player.fallDistance = 0;
            FabricDimensions.teleport(player, player.getWorld(), new TeleportTarget(hub.pos, new Vec3d(0, 0, 0), 0, 0));
        }
    }

    public static RegistryKey<World> redirect(RegistryKey<World> registryKey) {
        if (isOverworld(registryKey)) {
            return World.OVERWORLD;
        }
        if (isEnd(registryKey)) {
            return World.END;
        }
        if (isNether(registryKey)) {
            return World.NETHER;
        }
        return registryKey;
    }

    public static Optional<Island> getIsland(World world) {
        if (isIsland(world)) {
            return skylands.logic.Skylands.instance.islands.get(UUID.fromString(world.getRegistryKey().getValue().getPath()));
        }
        return Optional.empty();
    }

    public static Optional<Island> getIsland(PlayerEntity player) {
        return skylands.logic.Skylands.instance.islands.getFromMember(player);
    }

}
