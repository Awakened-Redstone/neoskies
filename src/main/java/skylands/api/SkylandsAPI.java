package skylands.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import skylands.SkylandsMain;
import skylands.logic.Island;
import skylands.logic.Skylands;
import skylands.util.Constants;

import java.util.Optional;
import java.util.UUID;

public class SkylandsAPI {
    static Skylands skylands = Skylands.getInstance();

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

    public static Optional<Island> getIsland(PlayerEntity player) {
        return skylands.islands.getFromMember(player);
    }

    public static Optional<Island> getIsland(String playerName) {
        return skylands.islands.get(playerName);
    }

    public static Optional<Island> getIsland(UUID playerUuid) {
        return skylands.islands.get(playerUuid);
    }

    public static Optional<Island> getIsland(World world) {
        if (SkylandsAPI.isIsland(world)) {
            return skylands.islands.get(UUID.fromString(world.getRegistryKey().getValue().getPath()));
        }
        return Optional.empty();
    }

    public static void setBalanceHandler(BalanceHandler handler) {
        skylands.balanceHandler = handler;
    }
}
