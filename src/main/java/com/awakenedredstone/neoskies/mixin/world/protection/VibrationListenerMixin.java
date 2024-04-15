package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import net.minecraft.entity.player.PlayerEntity;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

import java.util.Optional;

@Mixin(Vibrations.VibrationListener.class)
public abstract class VibrationListenerMixin {

    @Shadow
    public abstract PositionSource getPositionSource();

    @Inject(method = "listen*", at = @At("HEAD"), cancellable = true)
    private void accept(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient()) {
            Optional<Vec3d> optional = getPositionSource().getPos(world);
            if (optional.isEmpty()) return;
            BlockPos pos = BlockPos.ofFloored(optional.get());
            if ((emitter.sourceEntity() instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.INTERACT_SCULK))) {
                cir.setReturnValue(false);
            }
        }
    }
}
