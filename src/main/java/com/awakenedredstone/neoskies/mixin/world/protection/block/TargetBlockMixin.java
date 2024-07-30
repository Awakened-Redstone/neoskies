package com.awakenedredstone.neoskies.mixin.world.protection.block;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.util.ServerUtils;
import com.awakenedredstone.neoskies.util.WorldProtection;
import net.minecraft.block.BlockState;
import net.minecraft.block.TargetBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TargetBlock.class)
public class TargetBlockMixin {
    @Inject(method = "onProjectileHit", at = @At("HEAD"), cancellable = true)
    private void useOnBlock(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile, CallbackInfo ci) {
        if (!world.isClient()) {
            if (projectile.getOwner() instanceof PlayerEntity player) {
                if (!WorldProtection.canModify(world, hit.getBlockPos(), player, NeoSkiesIslandSettings.USE_REDSTONE)) {
                    ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.USE_REDSTONE);
                    ci.cancel();
                }
            }
        }
    }
}
