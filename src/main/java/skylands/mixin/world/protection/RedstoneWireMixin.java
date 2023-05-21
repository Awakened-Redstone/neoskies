package skylands.mixin.world.protection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireMixin extends Block {

    public RedstoneWireMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    void preventInteraction(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient) {
            if (!WorldProtection.canModify(world, pos, player)) {
                protectionWarning(player, "redstone");
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    void preventActivation(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!world.isClient) {
            if (!WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    void preventChange(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        if (!world.isClient()) {
            if (!WorldProtection.isWithinIsland((World) world, pos) || !WorldProtection.isWithinIsland((World) world, neighborPos)) {
                cir.setReturnValue(state);
            }
        }
    }

    @Inject(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At("HEAD"), cancellable = true)
    private void preventConnection(BlockView world, BlockPos pos, Direction direction, boolean bl, CallbackInfoReturnable<WireConnection> cir) {
        if (!((World) world).isClient) {
            if (!WorldProtection.isWithinIsland((World) world, pos.offset(direction))) {
                cir.setReturnValue(WireConnection.NONE);
            }
        }
    }
}
