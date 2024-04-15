package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.util.ServerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.api.SkylandsAPI;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!getWorld().isClient()) {
            if ((LivingEntity) (Object) this instanceof PlayerEntity player) {
                if (!WorldProtection.canModify(getWorld(), this.getBlockPos(), player) && !source.equals(getWorld().getDamageSources().outOfWorld()) && !SkylandsAPI.isHub(getWorld())) {
                    cir.setReturnValue(false);
                }
            }
            if (source.getAttacker() instanceof PlayerEntity attacker) {
                System.out.println(attacker.getDisplayName());
                System.out.println(this.getBlockPos());
                System.out.println(WorldProtection.canModify(getWorld(), this.getBlockPos(), attacker));
                if (!WorldProtection.canModify(getWorld(), this.getBlockPos(), attacker)) {
                    ServerUtils.protectionWarning(attacker, "entity_hurt");
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
