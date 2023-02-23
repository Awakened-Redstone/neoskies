package skylands.mixin.world.protection;

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
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;

@Mixin(LeashKnotEntity.class)
public abstract class LeashKnotEntityMixin extends DecorationEntityMixin {

    public LeashKnotEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient && player != null) {
            if (!WorldProtection.canModify(world, attachmentPos, player)) {
                protectionWarning(player, "leash");
                player.getInventory().updateItems();
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
