package com.awakenedredstone.neoskies.mixin.world.protection.block;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.util.ServerUtils;
import com.awakenedredstone.neoskies.util.WorldProtection;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TntBlock.class)
public class TntBlockMixin {
    @Inject(method = "primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private static void primeTnt(World world, BlockPos pos, LivingEntity igniter, CallbackInfo ci) {
        if (!world.isClient()) {
            PlayerEntity player = null;

            if (igniter instanceof PlayerEntity) {
                player = (PlayerEntity) igniter;
            } else if (igniter instanceof Ownable ownable && ownable.getOwner() instanceof PlayerEntity owner) {
                player = owner;
            }

            if (player == null) {
                return;
            }

            if (!WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.USE_TNT, NeoSkiesIslandSettings.BREAK_BLOCKS)) {
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.USE_TNT);
                ci.cancel();
            }
        }
    }

    @Inject(method = "onProjectileHit", at = @At("HEAD"), cancellable = true)
    private void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile, CallbackInfo ci) {
        if (!world.isClient()) {
            PlayerEntity player = null;

            if (projectile.getOwner() instanceof PlayerEntity owner) {
                player = owner;
            }

            if (player == null) {
                return;
            }

            if (!WorldProtection.canModify(world, hit.getBlockPos(), player, NeoSkiesIslandSettings.USE_TNT, NeoSkiesIslandSettings.BREAK_BLOCKS)) {
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.USE_TNT);
                ci.cancel();
            }
        }
    }

    @Inject(method = "onDestroyedByExplosion", at = @At("HEAD"), cancellable = true)
    private void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion, CallbackInfo ci) {
        if (!world.isClient()) {
            LivingEntity causingEntity = explosion.getCausingEntity();
            if (causingEntity instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.USE_TNT, NeoSkiesIslandSettings.BREAK_BLOCKS)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onUseWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/TntBlock;primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void onUse(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ItemActionResult> cir) {
        if (!world.isClient()) {
            if (!WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.USE_TNT, NeoSkiesIslandSettings.BREAK_BLOCKS)) {
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.USE_TNT);
                int slot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                cir.setReturnValue(ItemActionResult.FAIL);
            }
        }
    }
}
