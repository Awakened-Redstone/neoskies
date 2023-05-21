package skylands.mixin.world.protection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;

@Mixin(BoatEntity.class)
public abstract class BoatMixin extends Entity {

    public BoatMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    void preventDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient && source.getAttacker() instanceof PlayerEntity attacker) {
            if (!WorldProtection.canModify(world, getBlockPos(), attacker)) {
                protectionWarning(attacker, "entity_hurt");
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    void preventInteraction(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!WorldProtection.canModify(world, getBlockPos(), player)) {
            protectionWarning(player, "interact");
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
