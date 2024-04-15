package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.util.ServerUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {

    @Shadow
    @Final
    public static DirectionProperty FACING;

    @Inject(method = "dispense", at = @At("HEAD"), cancellable = true)
    private void dispense(ServerWorld world, BlockState state, BlockPos pos, CallbackInfo ci) {
        if (!world.isClient()) {
            if (!WorldProtection.isWithinIsland(world, pos) || !WorldProtection.isWithinIsland(world, pos.offset((world.getBlockState(pos).get(FACING))))) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient()) {
            if (!WorldProtection.canModify(world, pos, player)) {
                ServerUtils.protectionWarning(player, "dispenser_open");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
