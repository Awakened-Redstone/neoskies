package com.awakenedredstone.neoskies.util;

import com.awakenedredstone.neoskies.logic.Island;
import net.minecraft.entity.player.PlayerEntity;

public class IslandUtils {
    public static boolean islandOwner(PlayerEntity player, Island island) {
        return island.owner.uuid.equals(player.getUuid());
    }
}
