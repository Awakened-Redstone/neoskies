package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SculkCatalystBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.awakenedredstone.neoskies.util.WorldProtection;

import java.util.Optional;

@Mixin(SculkCatalystBlockEntity.Listener.class)
public abstract class SculkCatalystBlockEntityMixin {

    @Shadow
    public abstract PositionSource getPositionSource();

    @Inject(method = "listen", at = @At("HEAD"), cancellable = true)
    private void listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos, CallbackInfoReturnable<Boolean> cir) {
        Optional<Vec3d> optional = getPositionSource().getPos(world);
        if (optional.isEmpty()) return;
        if (!(emitter.sourceEntity() instanceof PlayerEntity player)) return;
        if (!world.isClient()) {
            if (!WorldProtection.canModify(world, BlockPos.ofFloored(optional.get()), player, NeoSkiesIslandSettings.INTERACT_SCULK)) {
                cir.setReturnValue(false);
            }
        }
    }
}
