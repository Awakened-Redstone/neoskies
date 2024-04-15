package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.util.ServerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(TntBlock.class)
public class TntBlockMixin {

    @Inject(method = "primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private static void primeTnt(World world, BlockPos pos, LivingEntity igniter, CallbackInfo ci) {
        if (!world.isClient()) {
            if (!WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onProjectileHit", at = @At("HEAD"), cancellable = true)
    private void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile, CallbackInfo ci) {
        if (!world.isClient()) {
            if (!WorldProtection.isWithinIsland(world, hit.getBlockPos())) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "neighborUpdate", at = @At("HEAD"), cancellable = true)
    private void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify, CallbackInfo ci) {
        if (!world.isClient()) {
            if (!WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onBlockAdded", at = @At("HEAD"), cancellable = true)
    private void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        if (!world.isClient()) {
            if (!WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onDestroyedByExplosion", at = @At("HEAD"), cancellable = true)
    private void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion, CallbackInfo ci) {
        if (!world.isClient()) {
            if (!WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onBreak", at = @At("HEAD"), cancellable = true)
    private void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<BlockState> cir) {
        if (!world.isClient()) {
            if (!WorldProtection.canModify(world, pos, player)) {
                ServerUtils.protectionWarning(player, "tnt_break");
                cir.setReturnValue(state);
            }
        }
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient()) {
            if (!WorldProtection.canModify(world, pos, player)) {
                ItemStack itemStack = player.getStackInHand(hand);
                if (!itemStack.isOf(Items.FLINT_AND_STEEL) && !itemStack.isOf(Items.FIRE_CHARGE)) ServerUtils.protectionWarning(player, "tnt_ignite");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
