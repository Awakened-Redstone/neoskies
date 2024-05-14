package com.awakenedredstone.neoskies.mixin.world.protection.block;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.util.WorldProtection;
import net.minecraft.block.entity.SculkCatalystBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SculkCatalystBlockEntity.Listener.class)
public abstract class SculkCatalystBlockEntityMixin {
    @Inject(method = "listen", at = @At("HEAD"), cancellable = true)
    private void listen(ServerWorld world, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter, Vec3d emitterPos, CallbackInfoReturnable<Boolean> cir) {
        BlockPos pos = BlockPos.ofFloored(emitterPos);
        if (!world.isClient()) {
            Entity entity = emitter.sourceEntity();
            if (!(entity instanceof LivingEntity living)) {
                return;
            }

            LivingEntity attacker = living.getLastAttacker();

            PlayerEntity player = null;

            if (attacker instanceof PlayerEntity) {
                player = (PlayerEntity) attacker;
            } else if (attacker instanceof Ownable ownable && ownable.getOwner() instanceof PlayerEntity owner) {
                player = owner;
            }

            if (player == null) {
                return;
            }

            if (!WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.INTERACT_SCULK)) {
                cir.setReturnValue(false);
            }
        }
    }
}
