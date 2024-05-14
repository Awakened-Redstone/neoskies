package com.awakenedredstone.neoskies.mixin.block;

import com.awakenedredstone.neoskies.api.SkylandsAPI;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.Skylands;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Inject(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void resourceKey(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (SkylandsAPI.isIsland(world)) {
            if (!Skylands.getConfig().enableEndIsland) {
                ci.cancel();
                return;
            }
            Optional<Island> island = SkylandsAPI.getIsland(world);
            if (island.isPresent()) {
                ServerWorld targetWorld;
                if (SkylandsAPI.isEnd(world.getRegistryKey())) {
                    targetWorld = island.get().getOverworld();
                } else {
                    targetWorld = island.get().getEnd();
                }
                if (targetWorld != null) {
                    entity.moveToWorld(targetWorld);
                }
                ci.cancel();
            }
        }
    }
}
