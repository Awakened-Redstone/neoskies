package com.awakenedredstone.neoskies.datagen;

import com.awakenedredstone.neoskies.logic.predicate.IslandLevelPredicate;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.FluidTags;

import java.util.concurrent.CompletableFuture;

public class NeoSkiesBlockGenProvider extends StandardBlockGenProvider {
    public NeoSkiesBlockGenProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate() {
        createGenerator("cobblestone", Fluids.FLOWING_LAVA, FluidTags.WATER)
          .addOutput(
            generatorSet()
              .addBlock(Blocks.COBBLESTONE, 20)
              .addBlock(Blocks.COAL_ORE, 5)
              .addBlock(Blocks.IRON_ORE, 3)
              .addBlock(Blocks.DIAMOND_ORE, 1, new IslandLevelPredicate(3, IslandLevelPredicate.Type.LEVEL, false))
          );
    }
}
