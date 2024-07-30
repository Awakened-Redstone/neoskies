package com.awakenedredstone.neoskies.test;

import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TestBlock extends SimplePolymerBlock {
    public static final EnumProperty<State> TEST_STATE = EnumProperty.of("test_state", State.class);

    public TestBlock(Settings settings) {
        super(settings, Blocks.RED_CONCRETE);
        this.setDefaultState(this.getStateManager().getDefaultState().with(TEST_STATE, State.NOT_WORKING));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TEST_STATE);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return (
          switch (state.get(TEST_STATE)) {
              case NOT_WORKING -> Blocks.RED_CONCRETE;
              case PARTIAL -> Blocks.YELLOW_CONCRETE;
              case WORKING -> Blocks.LIME_CONCRETE;
          }
        ).getDefaultState();
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isEmpty() && hand == Hand.MAIN_HAND) {
            world.setBlockState(pos, state.cycle(TEST_STATE), 0);
            player.swingHand(hand);
            return ItemActionResult.SUCCESS;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    public enum State implements StringIdentifiable {
        NOT_WORKING,
        PARTIAL,
        WORKING;

        @Override
        public String asString() {
            return this.name().toLowerCase();
        }
    }
}
