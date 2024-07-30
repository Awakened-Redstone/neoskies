package com.awakenedredstone.neoskies.util;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.api.island.PermissionLevel;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesPermissionLevels;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WorldProtection {
    /**
     * @deprecated Please use {@link WorldProtection#canModify(World, PlayerEntity, IslandSettings)}
     **/
    @Deprecated
    public static boolean canModify(@NotNull World world, @NotNull PlayerEntity player) {
        if (NeoSkies.PROTECTION_BYPASS.contains(player)) {
            if (Permissions.check(player, "neoskies.admin.protection.bypass", 4)) {
                return true;
            } else {
                NeoSkies.PROTECTION_BYPASS.remove(player);
            }
        }

        Optional<Island> island = NeoSkiesAPI.getOptionalIsland(world);
        if (island.isPresent() && !island.get().isMember(player)) {
            return false;
        }

        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            return !IslandLogic.getInstance().hub.hasProtection;
        }

        return true;
    }

    /**
     * @deprecated Please use {@link WorldProtection#canModify(World, BlockPos, PlayerEntity, IslandSettings)}
     **/
    @Deprecated
    public static boolean canModify(@NotNull World world, @NotNull BlockPos pos, @NotNull PlayerEntity player) {
        if (NeoSkies.PROTECTION_BYPASS.contains(player)) {
            if (Permissions.check(player, "neoskies.admin.protection.bypass", 4)) {
                return true;
            } else {
                NeoSkies.PROTECTION_BYPASS.remove(player);
            }
        }
        Optional<Island> island = NeoSkiesAPI.getOptionalIsland(world);
        if (island.isPresent()) {
            if (!island.get().isWithinBorder(pos)) {
                return false;
            } else if (island.get().isMember(player)) {
                return true;
            }
        }

        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            return !IslandLogic.getInstance().hub.hasProtection;
        }

        return false;
    }

    @SafeVarargs
    public static <T extends IslandSettings> boolean canModify(@NotNull World world, @NotNull BlockPos pos, @NotNull PlayerEntity player, @NotNull T... settings) {
        boolean allowed = true;
        for (T setting : settings) {
            if (!canModify(world, pos, player, setting)) {
                return false;
            }
        }
        return true;
    }

    public static <T extends IslandSettings> boolean canModify(@NotNull World world, @NotNull BlockPos pos, @NotNull PlayerEntity player, @NotNull T setting) {
        if (NeoSkies.PROTECTION_BYPASS.contains(player)) {
            if (Permissions.check(player, "neoskies.admin.protection.bypass", 4)) {
                return true;
            } else {
                NeoSkies.PROTECTION_BYPASS.remove(player);
            }
        }

        Optional<Island> island = NeoSkiesAPI.getOptionalIsland(world);
        if (island.isPresent()) {
            if (!island.get().isWithinBorder(pos)) {
                return false;
            }
            if (island.get().isInteractionAllowed(setting.getIdentifier(), getPlayerPermissionLevel(world, player))) {
                return true;
            }
        }

        if (world.getRegistryKey().equals(World.OVERWORLD) && IslandLogic.getInstance().hub.hasProtection) {
            return false;
        }

        return false;
    }

    public static <T extends IslandSettings> boolean canModify(@NotNull World world, @NotNull PlayerEntity player, @NotNull T setting) {
        if (NeoSkies.PROTECTION_BYPASS.contains(player)) {
            if (Permissions.check(player, "neoskies.admin.protection.bypass", 4)) {
                return true;
            } else {
                NeoSkies.PROTECTION_BYPASS.remove(player);
            }
        }

        Optional<Island> island = NeoSkiesAPI.getOptionalIsland(world);
        if (island.isPresent()) {
            if (island.get().isInteractionAllowed(setting.getIdentifier(), getPlayerPermissionLevel(world, player))) {
                return true;
            }
        }

        if (world.getRegistryKey().equals(World.OVERWORLD) && IslandLogic.getInstance().hub.hasProtection) {
            return false;
        }

        return false;
    }

    public static @NotNull PermissionLevel getPlayerPermissionLevel(@NotNull World world, @NotNull PlayerEntity player) {
        Optional<Island> island = NeoSkiesAPI.getOptionalIsland(world);
        if (island.isPresent() && island.get().isMember(player)) {
            if (island.get().owner.uuid == player.getUuid()) {
                return NeoSkiesPermissionLevels.OWNER;
            } else {
                return NeoSkiesPermissionLevels.MEMBER;
            }
        }

        return NeoSkiesPermissionLevels.VISITOR;
    }

    public static boolean isWithinIsland(@NotNull World world, @NotNull BlockPos pos) {
        Optional<Island> island = NeoSkiesAPI.getOptionalIsland(world);

        if (NeoSkiesAPI.isHub(world)) {
            return true;
        }

        if (island.isPresent() && (!island.get().isWithinBorder(pos))) {
            return false;
        }

        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            return !IslandLogic.getInstance().hub.hasProtection;
        }

        return true;
    }
}
