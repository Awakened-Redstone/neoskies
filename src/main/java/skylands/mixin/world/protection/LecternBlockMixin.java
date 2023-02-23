package skylands.mixin.world.protection;

import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.entity.player.PlayerEntity;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;

@Mixin(LecternBlock.class)
public class LecternBlockMixin {

    @Shadow
    @Final
    public static BooleanProperty HAS_BOOK;

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient) {
            if (!WorldProtection.canModify(world, pos, player) && !state.get(HAS_BOOK)) {
                protectionWarning(player, "lectern");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
