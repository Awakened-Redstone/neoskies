package skylands.mixin.world.protection;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SculkCatalystBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.util.WorldProtection;

@Mixin(SculkCatalystBlockEntity.class)
public abstract class SculkCatalystBlockEntityMixin extends BlockEntity {

    public SculkCatalystBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "listen", at = @At("HEAD"), cancellable = true)
    private void listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient) {
            if (!WorldProtection.isWithinIsland(world, pos)) {
                cir.setReturnValue(false);
            }
        }
    }
}
