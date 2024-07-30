package com.awakenedredstone.neoskies.mixin.world.protection.block;

import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Inject(method = "isMovable", at = @At("HEAD"), cancellable = true)
    private static void isMovable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir) {
        if ((!WorldProtection.isWithinIsland(world, pos.offset(direction)) || !WorldProtection.isWithinIsland(world, pos)) && !(state.getBlock() instanceof AirBlock)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void move(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir) {
        if ((!WorldProtection.isWithinIsland(world, pos.offset(dir)) || !WorldProtection.isWithinIsland(world, pos)) && !(world.getBlockState(pos).getBlock() instanceof AirBlock)) {
            cir.setReturnValue(false);
        }
    }
}
