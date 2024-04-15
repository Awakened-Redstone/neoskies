package com.awakenedredstone.neoskies.mixin.world.protection;

import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(SculkSpreadManager.Cursor.class)
public class SculkSpreadCursorMixin {

    @Inject(method = "spread", at = @At("HEAD"), cancellable = true)
    private void spread(WorldAccess world, BlockPos pos, Random random, SculkSpreadManager spreadManager, boolean shouldConvertToBlock, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            if (!WorldProtection.isWithinIsland(serverWorld, pos)) {
                ci.cancel();
            }
        }
    }
}
