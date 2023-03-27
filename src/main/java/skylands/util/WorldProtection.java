package skylands.util;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import skylands.SkylandsMain;
import skylands.api.SkylandsAPI;
import skylands.api.island.IslandSettingsList;
import skylands.api.island.PermissionLevel;
import skylands.logic.Island;
import skylands.logic.Skylands;

import java.util.Optional;

public class WorldProtection {

    public static boolean canModify(World world, PlayerEntity player) {
        if (SkylandsMain.PROTECTION_BYPASS.contains(player)) {
            if (Permissions.check(player, "skylands.admin.protection.bypass", 4)) return true;
            else SkylandsMain.PROTECTION_BYPASS.remove(player);
        }
        Optional<Island> island = SkylandsAPI.getIsland(world);
        if (island.isPresent() && !island.get().isMember(player)) {
            return false;
        }

        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            return !Skylands.instance.hub.hasProtection;
        }

        return true;
    }

    public static boolean canModify(World world, BlockPos pos, PlayerEntity player) {
        if (SkylandsMain.PROTECTION_BYPASS.contains(player)) {
            if (Permissions.check(player, "skylands.admin.protection.bypass", 4)) return true;
            else SkylandsMain.PROTECTION_BYPASS.remove(player);
        }
        Optional<Island> island = SkylandsAPI.getIsland(world);
        if (island.isPresent()) {
            if (!island.get().isWithinBorder(pos)) return false;
            else if (island.get().isMember(player)) return true;
        }

        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            return !Skylands.instance.hub.hasProtection;
        }

        return false;
    }

    public static <T extends IslandSettingsList> boolean canModify(World world, BlockPos pos, PlayerEntity player, T setting) {
        if (SkylandsMain.PROTECTION_BYPASS.contains(player)) {
            if (Permissions.check(player, "skylands.admin.protection.bypass", 4)) return true;
            else SkylandsMain.PROTECTION_BYPASS.remove(player);
        }

        Optional<Island> island = SkylandsAPI.getIsland(world);
        if (island.isPresent()) {
            if (!island.get().isWithinBorder(pos)) return false;
            if (island.get().isInteractionAllowed(setting.getIdentifier(), getPlayerPermissionLevel(world, player))) {
                return true;
            }
        }

        if (world.getRegistryKey().equals(World.OVERWORLD) && Skylands.instance.hub.hasProtection) {
            return false;
        }

        return false;
    }

    public static PermissionLevel getPlayerPermissionLevel(World world, PlayerEntity player) {
        Optional<Island> island = SkylandsAPI.getIsland(world);
        if (island.isPresent() && island.get().isMember(player)) {
            if (island.get().owner.uuid == player.getUuid()) return PermissionLevel.OWNER;
            else return PermissionLevel.MEMBER;
        }

        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            return PermissionLevel.VISITOR;
        }

        return PermissionLevel.VISITOR;
    }

    public static boolean isWithinIsland(World world, BlockPos pos) {
        Optional<Island> island = SkylandsAPI.getIsland(world);

        if (SkylandsAPI.isHub(world)) return true;

        if (island.isPresent() && (!island.get().isWithinBorder(pos))) {
            return false;
        }

        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            return !Skylands.instance.hub.hasProtection;
        }

        return true;
    }
}
