package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.util.ServerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(AbstractMinecartEntity.class)
public abstract class MinecartMixin extends Entity {

    public MinecartMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /*@Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!getWorld().isClient() && source.getAttacker() instanceof PlayerEntity attacker) {
            if (!WorldProtection.canModify(getWorld(), getBlockPos(), attacker)) {
                ServerUtils.protectionWarning(attacker, "entity_hurt");
                cir.setReturnValue(false);
            }
        }
    }*/
}
