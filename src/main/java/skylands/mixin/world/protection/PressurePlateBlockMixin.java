package skylands.mixin.world.protection;

import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;

@Mixin(AbstractPressurePlateBlock.class)
public class PressurePlateBlockMixin {

    @Inject(method = "updatePlateState", at = @At("HEAD"), cancellable = true)
    private void updatePlateState(Entity entity, World world, BlockPos pos, BlockState state, int output, CallbackInfo ci) {
        if (!world.isClient) {
            if ((entity instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player))) {
                protectionWarning(player, "redstone");
                ci.cancel();
            } else if (!WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }
}
