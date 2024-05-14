package com.awakenedredstone.neoskies.mixin.world.protection.block;

import blue.endless.jankson.annotation.Nullable;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.util.WorldProtection;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ButtonBlock.class)
public class ButtonBlockMixin {
    @Inject(method = "tryPowerWithProjectiles", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;", shift = At.Shift.BEFORE), cancellable = true)
    private void onProjectileHit(BlockState state, World world, BlockPos pos, CallbackInfo ci, @Nullable @Local PersistentProjectileEntity projectile) {
        if (projectile == null) return;
        if (!world.isClient()) {
            PlayerEntity player = null;
            if (projectile.getOwner() instanceof PlayerEntity owner) {
                player = owner;
            }

            if (player == null) {
                return;
            }

            if (!WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.USE_REDSTONE)) {
                ci.cancel();
            }
        }
    }
}
