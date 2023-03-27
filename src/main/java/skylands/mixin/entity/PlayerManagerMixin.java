package skylands.mixin.entity;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import skylands.api.SkylandsAPI;
import skylands.logic.Island;
import skylands.logic.Skylands;

import java.util.Optional;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getSpawnPointPosition()Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos skylands$respawnOnIsland(ServerPlayerEntity player) {
        if (SkylandsAPI.isIsland(player.world)) {
            Optional<Island> islandOptional = SkylandsAPI.getIsland(player.world);
            if (islandOptional.isPresent()) {
                Island island = islandOptional.get();
                if (island.isMember(player)) {
                    return BlockPos.ofFloored(island.spawnPos);
                } else {
                    return BlockPos.ofFloored(island.visitsPos);
                }
            }
        }
        return BlockPos.ofFloored(Skylands.getInstance().hub.pos);
    }
}
