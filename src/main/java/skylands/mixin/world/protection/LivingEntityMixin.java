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
import skylands.api.SkylandsAPI;
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient) {
            if ((LivingEntity) (Object) this instanceof PlayerEntity player) {
                if (!WorldProtection.canModify(world, this.getBlockPos(), player) && !source.equals(world.getDamageSources().outOfWorld()) && !SkylandsAPI.isHub(world)) {
                    cir.setReturnValue(false);
                }
            }
            if (source.getAttacker() instanceof PlayerEntity attacker) {
                System.out.println(attacker.getDisplayName());
                System.out.println(this.getBlockPos());
                System.out.println(WorldProtection.canModify(world, this.getBlockPos(), attacker));
                if (!WorldProtection.canModify(world, this.getBlockPos(), attacker)) {
                    protectionWarning(attacker, "entity_hurt");
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
