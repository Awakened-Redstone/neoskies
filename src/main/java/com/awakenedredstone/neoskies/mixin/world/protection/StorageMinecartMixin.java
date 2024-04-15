package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.util.ServerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(StorageMinecartEntity.class)
public class StorageMinecartMixin {

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!player.getWorld().isClient()) {
            if (!WorldProtection.canModify(player.getWorld(), player)) {
                ServerUtils.protectionWarning(player, "minecart_open");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }

}
