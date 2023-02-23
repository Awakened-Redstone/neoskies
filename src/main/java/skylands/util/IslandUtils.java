package skylands.util;

import net.minecraft.entity.player.PlayerEntity;
import skylands.logic.Island;

public class IslandUtils {

    public static boolean islandOwner(PlayerEntity player, Island island) {
        return island.owner.uuid.equals(player.getUuid());
    }
}
