package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.util.ServerUtils;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(AbstractPressurePlateBlock.class)
public class PressurePlateBlockMixin {

    @Inject(method = "updatePlateState", at = @At("HEAD"), cancellable = true)
    private void updatePlateState(Entity entity, World world, BlockPos pos, BlockState state, int output, CallbackInfo ci) {
        if (!world.isClient()) {
            if ((entity instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player))) {
                ServerUtils.protectionWarning(player, "redstone");
                ci.cancel();
            } else if (!WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }
}
