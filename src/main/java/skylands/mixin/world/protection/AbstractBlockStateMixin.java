package skylands.mixin.world.protection;

import net.minecraft.block.AbstractBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skylands.api.SkylandsAPI;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void disableRandomTickingOutsideIsland(ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (SkylandsAPI.isIsland(world) && !SkylandsAPI.getIsland(world).get().isWithinBorder(pos)) {
            ci.cancel();
        }
    }

    @Inject(method = "scheduledTick", at = @At("HEAD"), cancellable = true)
    private void disableTickingOutsideIsland(ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (SkylandsAPI.isIsland(world) && !SkylandsAPI.getIsland(world).get().isWithinBorder(pos)) {
            ci.cancel();
        }
    }
}
