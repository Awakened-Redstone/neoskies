package com.awakenedredstone.neoskies.mixin.world.protection.block;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.util.WorldProtection;
import net.minecraft.block.entity.SculkShriekerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkShriekerBlockEntity.VibrationCallback.class)
public class SculkShriekerBlockEntityMixin {
    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    private void accept(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, @Nullable Entity sourceEntity, @Nullable Entity entity, float distance, CallbackInfo ci) {
        if (!world.isClient()) {
            PlayerEntity player = null;

            if (entity instanceof PlayerEntity) {
                player = (PlayerEntity) entity;
            } else if (entity instanceof Ownable ownable && ownable.getOwner() instanceof PlayerEntity owner) {
                player = owner;
            }

            if (player == null) {
                return;
            }

            if (!WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.INTERACT_SCULK)) {
                ci.cancel();
            }
        }
    }
}
