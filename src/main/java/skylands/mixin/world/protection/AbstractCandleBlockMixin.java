package skylands.mixin.world.protection;

import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
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

@Mixin(AbstractCandleBlock.class)
public class AbstractCandleBlockMixin {

    @Inject(method = "onProjectileHit", at = @At("HEAD"), cancellable = true)
    private void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile, CallbackInfo ci) {
        if (!world.isClient) {
            BlockPos pos = hit.getBlockPos();
            if ((projectile.getOwner() instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player))) {
                if (projectile.isOnFire()) protectionWarning(player, "ignite");
                ci.cancel();
            } else if (!WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }
}
