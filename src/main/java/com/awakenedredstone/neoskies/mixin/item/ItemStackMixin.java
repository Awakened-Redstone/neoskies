package com.awakenedredstone.neoskies.mixin.item;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import com.awakenedredstone.neoskies.util.ServerUtils;
import com.awakenedredstone.neoskies.util.WorldProtection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract ActionResult useOnBlock(ItemUsageContext context);
    @Shadow public abstract Item getItem();

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = context.getStack();
        if (stack.getItem() instanceof BlockItem) return;

        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (!world.isClient() && player != null) {
            IslandSettings settings = null;

            for (Map.Entry<TagKey<Item>, IslandSettings> entry : NeoSkiesIslandSettings.getRuleItemTags().entrySet()) {
                if (stack.isIn(entry.getKey())) {
                    settings = entry.getValue();
                    break;
                }
            }

            if (settings == null) {
                return;
            }

            BlockPos blockPos = context.getBlockPos().offset(context.getSide());
            if (!WorldProtection.canModify(world, blockPos, player, settings)) {
                Hand hand = context.getHand();
                int slot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                ServerUtils.protectionWarning(player, settings);
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
