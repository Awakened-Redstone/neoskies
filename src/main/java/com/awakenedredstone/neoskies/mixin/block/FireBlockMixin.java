package com.awakenedredstone.neoskies.mixin.block;

import com.awakenedredstone.neoskies.util.Constants;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFireBlock.class)
public class FireBlockMixin {
    @Inject(method = "isOverworldOrNether", at = @At("HEAD"), cancellable = true)
    private static void isOverworldOrNether(World world, CallbackInfoReturnable<Boolean> cir) {
        if (world.getRegistryKey().getValue().getNamespace().equals(Constants.NAMESPACE) || world.getRegistryKey().getValue().getNamespace().equals(Constants.NAMESPACE_NETHER)) {
            cir.setReturnValue(true);
        }
    }
}
