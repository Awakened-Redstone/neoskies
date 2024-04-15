package com.awakenedredstone.neoskies.mixin.item;

import com.awakenedredstone.neoskies.util.ServerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.MinecartItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;
@Mixin(MinecartItem.class)
public class MinecartItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (!world.isClient() && player != null) {
            BlockPos blockPos = context.getBlockPos().offset(context.getSide());
            if (!WorldProtection.canModify(world, blockPos, player)) {
                ServerUtils.protectionWarning(player, "item_use");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
