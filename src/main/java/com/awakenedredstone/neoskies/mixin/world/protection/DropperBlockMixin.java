package com.awakenedredstone.neoskies.mixin.world.protection;

import net.minecraft.block.BlockState;
import net.minecraft.block.DropperBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(DropperBlock.class)
public class DropperBlockMixin extends DispenserBlockMixin {

    @Inject(method = "dispense", at = @At("HEAD"), cancellable = true)
    private void dispense(ServerWorld world, BlockState state, BlockPos pos, CallbackInfo ci) {
        if (!world.isClient()) {
            if (!WorldProtection.isWithinIsland(world, pos) || !WorldProtection.isWithinIsland(world, pos.offset((world.getBlockState(pos).get(FACING))))) {
                ci.cancel();
            }
        }
    }
}
