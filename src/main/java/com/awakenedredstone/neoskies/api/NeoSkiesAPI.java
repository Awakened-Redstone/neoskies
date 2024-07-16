package com.awakenedredstone.neoskies.api;

import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Constants;
import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class NeoSkiesAPI {
    public static boolean isHub(World world) {
        return world.getRegistryKey() == World.OVERWORLD;
    }

    public static boolean isProtectedArea(World world) {
        return isHub(world) || isIsland(world);
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
        return namespace.equals(Constants.NAMESPACE) || registryKey == World.OVERWORLD;
    }

    public static boolean isNether(RegistryKey<World> registryKey) {
        var namespace = registryKey.getValue().getNamespace();
        return namespace.equals(Constants.NAMESPACE_NETHER) || registryKey == World.NETHER;
    }

    public static boolean isEnd(RegistryKey<World> registryKey) {
        var namespace = registryKey.getValue().getNamespace();
        return namespace.equals(Constants.NAMESPACE_END) || registryKey == World.END;
    }

    public static Optional<Island> getIslandByPlayer(PlayerEntity player) {
        return IslandLogic.getInstance().islands.getFromMember(player);
    }

    public static Optional<Island> getIslandByPlayer(String playerName) {
        return IslandLogic.getInstance().islands.getByPlayer(playerName);
    }

    public static Optional<Island> getIslandByPlayer(UUID playerUuid) {
        return IslandLogic.getInstance().islands.getByPlayer(playerUuid);
    }

    public static Island getIsland(UUID islandId) throws NoSuchElementException {
        return getOptionalIsland(islandId).orElseThrow();
    }

    public static Optional<Island> getOptionalIsland(UUID islandId) {
        return IslandLogic.getInstance().islands.get(islandId);
    }

    public static Island getIsland(World world) throws NoSuchElementException {
        return getOptionalIsland(world).orElseThrow();
    }

    public static Optional<Island> getOptionalIsland(World world) {
        return getOptionalIsland(world.getRegistryKey());
    }

    public static Island getIsland(RegistryKey<World> registryKey) throws NoSuchElementException {
        return getOptionalIsland(registryKey).orElseThrow();
    }

    public static Optional<Island> getOptionalIsland(RegistryKey<World> registryKey) {
        if (NeoSkiesAPI.isIsland(registryKey)) {
            return IslandLogic.getInstance().islands.getByPlayer(UUID.fromString(registryKey.getValue().getPath()));
        }
        return Optional.empty();
    }

    public static boolean hasIsland(PlayerEntity player) {
        return getIslandByPlayer(player).isPresent();
    }

    public static boolean isIslandOwner(PlayerEntity player) {
        return getIslandByPlayer(player).isPresent() && getIslandByPlayer(player).get().owner.uuid.equals(player.getUuid());
    }

    public static @Nullable EconomyAccount getIslandWallet(Island island) {
        return island.getWallet();
    }
}
