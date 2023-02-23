package skylands.mixin.world.protection;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin extends AbstractDecorationEntity {

    protected ItemFrameEntityMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient && source.getAttacker() instanceof PlayerEntity attacker) {
            if (!WorldProtection.canModify(world, attacker)) {
                protectionWarning(attacker, "entity_hurt");
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!player.world.isClient) {
            if (!WorldProtection.canModify(world, this.getBlockPos(), player)) {
                protectionWarning(player, "item_frame_use");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
