package skylands.mixin.world.protection;

import net.minecraft.block.BlockState;
import net.minecraft.block.SculkCatalystBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skylands.util.WorldProtection;

@Mixin(SculkCatalystBlock.class)
public class SculkCatalystBlockMixin {

    @Inject(method = "bloom", at = @At("HEAD"), cancellable = true)
    private static void bloom(ServerWorld world, BlockPos pos, BlockState state, Random random, CallbackInfo ci) {
        if (!world.isClient) {
            if (!WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }
}
