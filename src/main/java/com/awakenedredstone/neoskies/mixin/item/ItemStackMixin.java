package com.awakenedredstone.neoskies.mixin.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract ActionResult useOnBlock(ItemUsageContext context);

    @Shadow
    public abstract Item getItem();

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (getItem() instanceof BlockItem) return;

        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (!world.isClient() && player != null) {
            BlockPos blockPos = context.getBlockPos().offset(context.getSide());
            if (!WorldProtection.canModify(world, blockPos, player)) {
                //ServerUtils.protectionWarning(player, "item_use");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
