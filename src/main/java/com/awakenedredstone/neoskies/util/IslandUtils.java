package com.awakenedredstone.neoskies.util;

import net.minecraft.entity.player.PlayerEntity;
import com.awakenedredstone.neoskies.logic.Island;

public class IslandUtils {

    public static boolean islandOwner(PlayerEntity player, Island island) {
        return island.owner.uuid.equals(player.getUuid());
    }
}
