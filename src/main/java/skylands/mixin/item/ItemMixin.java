package skylands.mixin.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.util.WorldProtection;
@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if ((Object) this instanceof BlockItem) return;
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (!world.isClient && player != null) {
            BlockPos blockPos = context.getBlockPos().offset(context.getSide());
            if (!WorldProtection.canModify(world, blockPos, player)) {
                //protectionWarning(player, "item_use");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
