package com.awakenedredstone.neoskies.datagen;

import com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class NeoSkiesBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public NeoSkiesBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(NeoSkiesBlockTags.ANVIL)
          .forceAddTag(BlockTags.ANVIL);

        getOrCreateTagBuilder(NeoSkiesBlockTags.BEACON)
          .add(Blocks.BEACON);

        getOrCreateTagBuilder(NeoSkiesBlockTags.BREWING_STAND)
          .add(Blocks.BREWING_STAND);

        getOrCreateTagBuilder(NeoSkiesBlockTags.COMPOSTER)
          .add(Blocks.COMPOSTER);

        getOrCreateTagBuilder(NeoSkiesBlockTags.CONTAINERS)
          .forceAddTag(BlockTags.CAMPFIRES)
          .forceAddTag(ConventionalBlockTags.CHESTS)
          .forceAddTag(ConventionalBlockTags.WOODEN_BARRELS)
          .forceAddTag(ConventionalBlockTags.SHULKER_BOXES)
          .add(
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.FURNACE,
            Blocks.BLAST_FURNACE,
            Blocks.SMOKER,
            Blocks.HOPPER,
            Blocks.CHISELED_BOOKSHELF,
            Blocks.DECORATED_POT,
            Blocks.JUKEBOX,
            Blocks.SPAWNER
          );

        getOrCreateTagBuilder(NeoSkiesBlockTags.DOORS)
          .forceAddTag(BlockTags.DOORS)
          .forceAddTag(BlockTags.TRAPDOORS)
          .forceAddTag(BlockTags.FENCE_GATES);

        getOrCreateTagBuilder(NeoSkiesBlockTags.LECTERN)
          .add(Blocks.LECTERN);

        getOrCreateTagBuilder(NeoSkiesBlockTags.LODESTONE)
          .add(Blocks.LODESTONE);

        getOrCreateTagBuilder(NeoSkiesBlockTags.REDSTONE)
          .forceAddTag(BlockTags.BUTTONS)
          .forceAddTag(BlockTags.PRESSURE_PLATES)
          .add(
            Blocks.REDSTONE_WIRE,
            Blocks.REPEATER,
            Blocks.COMPARATOR,
            Blocks.NOTE_BLOCK,
            Blocks.LEVER,
            Blocks.DAYLIGHT_DETECTOR
          );

        getOrCreateTagBuilder(NeoSkiesBlockTags.RESPAWN_ANCHOR)
          .add(Blocks.RESPAWN_ANCHOR);
    }
}
