package skylands.mixin.world.protection;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;

@Mixin(WrittenBookItem.class)
public class WrittenBookItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (!world.isClient && player != null) {
            if (!WorldProtection.canModify(world, context.getBlockPos(), player)) {
                protectionWarning(player, "lectern");
                player.getInventory().updateItems();
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}