package skylands.mixin.world.protection;

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
import skylands.logic.Skylands;
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient && world.getServer() != null) {
            if ((LivingEntity) (Object) this instanceof PlayerEntity player) {
                //Keep it as a failsafe, better than adding extra code to ticking :P
                if (!WorldProtection.canModify(world, player)) {
                    protectionWarning(player, "damage_take");
                    if (source.equals(world.getDamageSources().outOfWorld())) {
                        Skylands.instance.hub.visit(player);
                    }
                    cir.setReturnValue(false);
                }
            }
            if (source.getAttacker() instanceof PlayerEntity attacker) {
                if (!WorldProtection.canModify(world, attacker)) {
                    protectionWarning(attacker, "entity_hurt");
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
