package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.util.ServerUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandMixin extends LivingEntity {

    protected ArmorStandMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!getWorld().isClient() && source.getAttacker() instanceof PlayerEntity attacker) {
            if (!WorldProtection.canModify(getWorld(), this.getBlockPos(), attacker)) {
                ServerUtils.protectionWarning(attacker, "entity_hurt");
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    void interactAt(PlayerEntity player, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!player.getWorld().isClient()) {
            if (!WorldProtection.canModify(player.getWorld(), BlockPos.ofFloored(hitPos), player)) {
                ServerUtils.protectionWarning(player, "armor_stand_use");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }

    @Inject(method = "equip", at = @At("HEAD"), cancellable = true)
    void equip(PlayerEntity player, EquipmentSlot slot, ItemStack stack, Hand hand, CallbackInfoReturnable<Boolean> cir) {
        if (!player.getWorld().isClient()) {
            if (!WorldProtection.canModify(player.getWorld(), this.getBlockPos(), player)) {
                ServerUtils.protectionWarning(player, "armor_stand_use");
                cir.setReturnValue(false);
            }
        }
    }
}
