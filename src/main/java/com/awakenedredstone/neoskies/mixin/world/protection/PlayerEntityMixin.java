package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.util.ServerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "canPlaceOn", at = @At("HEAD"), cancellable = true)
    private void canPlaceOn(BlockPos pos, Direction facing, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        World world = player.getWorld();
        if (!world.isClient()) {
            BlockPos blockPos = pos.offset(facing);
            if (!WorldProtection.canModify(world, blockPos, player)) {
                ServerUtils.protectionWarning(player, "place_on");
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "canModifyBlocks", at = @At("HEAD"), cancellable = true)
    private void canModifyBlocks(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        World world = player.getWorld();
        if (!world.isClient()) {
            if (!WorldProtection.canModify(world, player)) {
                ServerUtils.protectionWarning(player, "modify");
                cir.setReturnValue(false);
            }
        }
    }
}
