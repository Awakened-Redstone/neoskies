package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.util.ServerUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(LeashKnotEntity.class)
public abstract class LeashKnotEntityMixin extends DecorationEntityMixin {

    public LeashKnotEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!getWorld().isClient() && player != null) {
            if (!WorldProtection.canModify(getWorld(), attachmentPos, player)) {
                ServerUtils.protectionWarning(player, "leash");
                player.getInventory().updateItems();
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
