package skylands.mixin.world.protection;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.SkylandsMain;
import skylands.util.WorldProtection;

@Mixin(World.class)
public class WorldMixin {

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("HEAD"), cancellable = true)
    private void setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        if (!SkylandsMain.MAIN_CONFIG.getConfig().disableBlocksOutsideIslands) return;
        if (!WorldProtection.isWithinIsland((World) (Object) this, pos)) cir.setReturnValue(false);
    }
}
