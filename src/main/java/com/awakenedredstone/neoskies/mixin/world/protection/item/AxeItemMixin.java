package com.awakenedredstone.neoskies.mixin.world.protection.item;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.util.ServerUtils;
import com.awakenedredstone.neoskies.util.WorldProtection;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin({AxeItem.class})
public class AxeItemMixin {
    @ModifyExpressionValue(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/AxeItem;tryStrip(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/block/BlockState;)Ljava/util/Optional;"))
    private Optional<BlockState> useOnBlock(Optional<BlockState> original, ItemUsageContext context, @Local World world, @Local PlayerEntity player, @Local BlockPos pos) {
        if (!world.isClient() && player != null && original.isPresent()) {
            if (!WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.PLACE_BLOCKS)) {
                Hand hand = context.getHand();
                ItemStack stack = context.getStack();
                int slot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.PLACE_BLOCKS);
                return Optional.empty();
            }
        }

        return original;
    }
}
