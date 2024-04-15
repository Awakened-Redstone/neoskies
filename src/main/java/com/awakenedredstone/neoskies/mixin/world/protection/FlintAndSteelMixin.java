package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.util.ServerUtils;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos blockPos = context.getBlockPos();
        if (!world.isClient() && player != null) {
            if (!WorldProtection.canModify(world, blockPos.offset(context.getSide()), player)) {
                if (!(world.getBlockState(blockPos).getBlock() instanceof TntBlock))
                    ServerUtils.protectionWarning(player, "flint_and_steel_use");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
