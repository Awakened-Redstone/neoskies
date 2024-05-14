package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.util.WorldProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.Vibrations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Vibrations.VibrationListener.class)
public abstract class VibrationListenerMixin {
    @Shadow public abstract PositionSource getPositionSource();

    @Inject(method = "listen(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/world/event/GameEvent$Emitter;Lnet/minecraft/util/math/Vec3d;)Z", at = @At("HEAD"), cancellable = true)
    private void manageVibrations0(ServerWorld world, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter, Vec3d emitterPos, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient()) {
            Optional<Vec3d> optional = getPositionSource().getPos(world);
            if (optional.isEmpty()) return;
            BlockPos pos = BlockPos.ofFloored(optional.get());
            if ((emitter.sourceEntity() instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.INTERACT_SCULK))) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "listen(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/event/Vibrations$ListenerData;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/world/event/GameEvent$Emitter;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
    private void manageVibrations1(ServerWorld world, Vibrations.ListenerData listenerData, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter, Vec3d emitterPos, Vec3d listenerPos, CallbackInfo ci) {
        if (!world.isClient()) {
            Optional<Vec3d> optional = getPositionSource().getPos(world);
            if (optional.isEmpty()) return;
            BlockPos pos = BlockPos.ofFloored(optional.get());
            Entity entity = emitter.sourceEntity();

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
