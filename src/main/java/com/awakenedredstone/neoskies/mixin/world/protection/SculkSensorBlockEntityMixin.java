package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(SculkSensorBlockEntity.VibrationCallback.class)
public class SculkSensorBlockEntityMixin {

    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    private void accept(ServerWorld world, BlockPos pos, GameEvent event, Entity sourceEntity, Entity entity, float distance, CallbackInfo ci) {
        if (!world.isClient()) {
            if ((entity instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.INTERACT_SCULK))) {
                ci.cancel();
            }
        }
    }
}
