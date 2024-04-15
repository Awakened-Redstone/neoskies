package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.util.ServerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(DecorationItem.class)
public class DecorationItemMixin extends Item {

    public DecorationItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (!world.isClient() && player != null) {
            if (!WorldProtection.canModify(world, context.getBlockPos().offset(context.getSide()), player)) {
                ServerUtils.protectionWarning(player, "item_place");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
