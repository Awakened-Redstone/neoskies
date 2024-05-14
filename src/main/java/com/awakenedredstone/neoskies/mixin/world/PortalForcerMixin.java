package com.awakenedredstone.neoskies.mixin.world;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.api.SkylandsAPI;
import com.awakenedredstone.neoskies.logic.Island;

import java.util.Optional;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {

    @Shadow @Final private ServerWorld world;

    @Inject(method = "createPortal", at = @At("HEAD"), cancellable = true)
    private void fixPortals(BlockPos pos, Direction.Axis axis, CallbackInfoReturnable<Optional<BlockLocating.Rectangle>> cir) {
        if (SkylandsAPI.isIsland(world)) {
            Island island = SkylandsAPI.getIsland(world).get();
            if (!island.isWithinBorder(pos) || SkylandsAPI.isEnd(world.getRegistryKey())) {
                cir.setReturnValue(Optional.empty());
            }
        }
    }
}