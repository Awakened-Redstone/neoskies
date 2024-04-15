package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(SculkSensorBlock.class)
public class SculkSensorBlockMixin {

    @Inject(method = "setActive", at = @At("HEAD"), cancellable = true)
    private void setActive(Entity sourceEntity, World world, BlockPos pos, BlockState state, int power, int frequency, CallbackInfo ci) {
        if (!world.isClient()) {
            if ((sourceEntity instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.INTERACT_SCULK))) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onSteppedOn", at = @At("HEAD"), cancellable = true)
    private void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (!world.isClient()) {
            if ((entity instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.INTERACT_SCULK))) {
                ci.cancel();
            }
        }
    }
}
