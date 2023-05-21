package skylands.mixin.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.logic.registry.SkylandsIslandSettings;
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;
@Mixin(BlockItem.class)
public class BlockItemMixin extends Item {

    public BlockItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (player instanceof ServerPlayerEntity serverPlayer) {
            BlockPos blockPos = context.getBlockPos().offset(context.getSide());
            if (!WorldProtection.canModify(world, blockPos, player, SkylandsIslandSettings.PLACE)) {
                protectionWarning(player, "block_place");
                int slot = context.getHand() == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                var stack = context.getStack();
                serverPlayer.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
