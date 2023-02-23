package skylands.mixin.world.protection;

import net.minecraft.block.BlockState;
import net.minecraft.block.TargetBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;

@Mixin(TargetBlock.class)
public class TargetBlockMixin {

    @Inject(method = "onProjectileHit", at = @At("HEAD"), cancellable = true)
    private void useOnBlock(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile, CallbackInfo ci) {
        TargetBlock block = (TargetBlock) ((Object) this);
        if (!world.isClient) {
            if (projectile.getOwner() instanceof PlayerEntity player) {
                if (!WorldProtection.canModify(world, hit.getBlockPos(), player)) {
                    protectionWarning(player, "target");
                    ci.cancel();
                }
            }

            if (!WorldProtection.isWithinIsland(world, new BlockPos(hit.getPos()))) {
                ci.cancel();
            }
        }
    }
}
