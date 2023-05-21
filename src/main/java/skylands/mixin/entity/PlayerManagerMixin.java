package skylands.mixin.entity;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getSpawnPointDimension()Lnet/minecraft/registry/RegistryKey;"))
    private RegistryKey<World> skylands$fixRespawnDimension(ServerPlayerEntity player) {
        return player.world.getRegistryKey();
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;findRespawnPosition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;FZZ)Ljava/util/Optional;"))
    private Optional<Vec3d> skylands$respawnOnIsland(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive) {
        return Optional.of(pos.toCenterPos());
    }
}
