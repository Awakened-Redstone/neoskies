package skylands.mixin.world.protection;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.VibrationListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.util.WorldProtection;

import java.util.Optional;

@Mixin(VibrationListener.class)
public class VibrationListenerMixin {

    @Shadow
    @Final
    protected PositionSource positionSource;

    @Inject(method = "listen", at = @At("HEAD"), cancellable = true)
    private void accept(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient) {
            Optional<Vec3d> optional = positionSource.getPos(world);
            if (optional.isEmpty()) return;
            BlockPos pos = new BlockPos(optional.get());
            if ((emitter.sourceEntity() instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player)) || !WorldProtection.isWithinIsland(world, pos)) {
                cir.setReturnValue(false);
            }
        }
    }
}
