package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import com.awakenedredstone.neoskies.util.ServerUtils;
import com.awakenedredstone.neoskies.util.WorldProtection;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"), cancellable = true)
    private void onUse(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient()) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            IslandSettings settings = null;

            for (Map.Entry<TagKey<Block>, IslandSettings> entry : NeoSkiesIslandSettings.getRuleBlockTags().entrySet()) {
                if (state.isIn(entry.getKey())) {
                    settings = entry.getValue();
                    break;
                }
            }

            if (settings == null) {
                return;
            }

            if (!WorldProtection.canModify(world, pos, player, settings)) {
                ServerUtils.protectionWarning(player, settings.getTranslationKey());
                int slot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUseWithItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ItemActionResult;"), cancellable = true)
    private void onUseWithItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient()) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            IslandSettings settings = null;

            for (Map.Entry<TagKey<Block>, IslandSettings> entry : NeoSkiesIslandSettings.getRuleBlockUseTag().entrySet()) {
                if (state.isIn(entry.getKey())) {
                    settings = entry.getValue();
                    break;
                }
            }

            if (settings == null) {
                return;
            }

            if (!WorldProtection.canModify(world, pos, player, settings)) {
                ServerUtils.protectionWarning(player, settings.getTranslationKey());
                int slot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
