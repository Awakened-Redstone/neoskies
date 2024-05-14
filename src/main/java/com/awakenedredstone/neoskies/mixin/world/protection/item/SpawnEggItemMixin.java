package com.awakenedredstone.neoskies.mixin.world.protection.item;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.util.ServerUtils;
import com.awakenedredstone.neoskies.util.WorldProtection;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnEggItem.class)
public class SpawnEggItemMixin {
    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;spawnFromItemStack(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnReason;ZZ)Lnet/minecraft/entity/Entity;"), cancellable = true)
    private void preventSpawn(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir, @Local BlockPos pos, @Local World world, @Local ItemStack stack) {
        PlayerEntity player = context.getPlayer();
        if (!world.isClient() && player != null) {
            if (!WorldProtection.canModify(world, context.getBlockPos(), player, NeoSkiesIslandSettings.USE_SPAWNER)) {
                Hand hand = context.getHand();
                int slot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.USE_SPAWNER);
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;spawnFromItemStack(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnReason;ZZ)Lnet/minecraft/entity/Entity;"), cancellable = true)
    private void preventSpawn(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir, @Local BlockPos pos, @Local ItemStack stack) {
        if (!world.isClient() && player != null) {
            if (!WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.USE_SPAWNER)) {
                int slot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.USE_SPAWNER);
                cir.setReturnValue(TypedActionResult.fail(stack));
            }
        }
    }
}
