package com.awakenedredstone.neoskies.mixin;

import com.awakenedredstone.neoskies.data.BlockGeneratorLoader;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin {
    @Shadow @Final protected FlowableFluid fluid;
    @Shadow @Final public static ImmutableList<Direction> FLOW_DIRECTIONS;

    @Shadow protected abstract void playExtinguishSound(WorldAccess world, BlockPos pos);

    /**
     * @author NeoSkies
     * @reason Data driven generators
     */
    @Inject(method = "receiveNeighborFluids", at = @At("HEAD"), cancellable = true)
    private void receiveNeighborFluids(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        Identifier sourceFluidId = Registries.FLUID.getId(world.getFluidState(pos).getFluid());

        if (BlockGeneratorLoader.INSTANCE.generate(sourceFluidId, world, pos)) {
            this.playExtinguishSound(world, pos);
            cir.setReturnValue(false);
        }
    }
}
