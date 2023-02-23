package skylands.mixin.world.protection;

import net.minecraft.block.BlockState;
import net.minecraft.block.SculkShriekerBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skylands.util.WorldProtection;

@Mixin(SculkShriekerBlock.class)
public class SculkShriekerBlockMixin {

    @Inject(method = "onSteppedOn", at = @At("HEAD"), cancellable = true)
    private void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (!world.isClient) {
            if ((entity instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player)) || !WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }
}