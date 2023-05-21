package skylands.mixin.world.protection;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HopperBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
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

@Mixin(HopperBlock.class)
public abstract class HopperBlockMixin extends BlockWithEntity {
    @Shadow @Final public static BooleanProperty ENABLED;
    @Shadow @Final public static DirectionProperty FACING;

    protected HopperBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    public void blockOpening(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient) {
            if (!WorldProtection.canModify(world, pos, player)) {
                protectionWarning(player, "hopper_open");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }

    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    private BlockState lockHopper(BlockState original, ItemPlacementContext ctx) {
        return original.with(ENABLED, WorldProtection.isWithinIsland(ctx.getWorld(), ctx.getBlockPos()));
    }

    @Inject(method = "updateEnabled", at = @At("HEAD"), cancellable = true)
    void lockHopper(World world, BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        if (!world.isClient) {
            if (!WorldProtection.isWithinIsland(world, pos)) {
                world.setBlockState(pos, state.with(ENABLED, false), flags);
                ci.cancel();
            }
        }
    }
}
