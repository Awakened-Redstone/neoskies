package skylands.mixin.world.protection;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {

    @Shadow
    @Final
    public static BooleanProperty LIT;

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient) {
            if (!WorldProtection.canModify(world, pos, player)) {
                if (state.get(LIT)) protectionWarning(player, "interact");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }

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
