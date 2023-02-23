package skylands.mixin.world.protection;

import net.minecraft.block.BlockState;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skylands.util.WorldProtection;

@Mixin(SculkSensorBlock.class)
public class SculkSensorBlockMixin {

    @Inject(method = "setActive", at = @At("HEAD"), cancellable = true)
    private static void setActive(Entity entity, World world, BlockPos pos, BlockState state, int power, CallbackInfo ci) {
        if (!world.isClient) {
            if ((entity instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player)) || !WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onSteppedOn", at = @At("HEAD"), cancellable = true)
    private void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (!world.isClient) {
            if ((entity instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player)) || !WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }
}