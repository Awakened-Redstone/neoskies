package skylands.mixin.world.protection;

import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skylands.util.WorldProtection;

@Mixin(SculkSensorBlockEntity.class)
public class SculkSensorBlockEntityMixin {

    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    private void accept(ServerWorld world, GameEventListener listener, BlockPos pos, GameEvent event, Entity entity, Entity sourceEntity, float distance, CallbackInfo ci) {
        if (!world.isClient) {
            if ((entity instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player)) || !WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }
}
