package com.awakenedredstone.neoskies.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.ANVIL;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.BEACON;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.BREWING_STAND;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.COMPOSTER;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.CONTAINERS;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.CONTAINERS_WITH_ITEM;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.COPPER_BLOCKS;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.DOORS;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.DRAGON_EGG;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.EXTINGUISHABLE;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.HARVEST;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.LECTERN;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.LODESTONE;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.OTHERS;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.REDSTONE;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.RESPAWN_ANCHOR;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.SIGNS;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.SPAWNER;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.UNWAXED_COPPER_BLOCKS;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags.WAXED_COPPER_BLOCKS;

public class NeoSkiesBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public NeoSkiesBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(ANVIL)
          .forceAddTag(BlockTags.ANVIL);

        getOrCreateTagBuilder(BEACON)
          .add(Blocks.BEACON);

        getOrCreateTagBuilder(BREWING_STAND)
          .add(Blocks.BREWING_STAND);

        getOrCreateTagBuilder(COMPOSTER)
          .add(Blocks.COMPOSTER);

        getOrCreateTagBuilder(CONTAINERS_WITH_ITEM)
          .forceAddTag(BlockTags.CAMPFIRES)
          .add(
            Blocks.CHISELED_BOOKSHELF,
            Blocks.DECORATED_POT,
            Blocks.JUKEBOX,
            Blocks.VAULT
          );

        getOrCreateTagBuilder(CONTAINERS)
          .forceAddTag(ConventionalBlockTags.CHESTS)
          .forceAddTag(ConventionalBlockTags.BARRELS)
          .forceAddTag(ConventionalBlockTags.SHULKER_BOXES)
          .addTag(CONTAINERS_WITH_ITEM)
          .add(
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.FURNACE,
            Blocks.BLAST_FURNACE,
            Blocks.SMOKER,
            Blocks.HOPPER,
            Blocks.BEEHIVE,
            Blocks.BEE_NEST,
            Blocks.SUSPICIOUS_GRAVEL,
            Blocks.SUSPICIOUS_SAND
          );

        getOrCreateTagBuilder(DOORS)
          .forceAddTag(BlockTags.DOORS)
          .forceAddTag(BlockTags.TRAPDOORS)
          .forceAddTag(BlockTags.FENCE_GATES);

        getOrCreateTagBuilder(LECTERN)
          .add(Blocks.LECTERN);

        getOrCreateTagBuilder(LODESTONE)
          .add(Blocks.LODESTONE);

        getOrCreateTagBuilder(REDSTONE)
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

        getOrCreateTagBuilder(RESPAWN_ANCHOR)
          .add(Blocks.RESPAWN_ANCHOR);

        getOrCreateTagBuilder(SPAWNER)
          .add(
            Blocks.SPAWNER,
            Blocks.TRIAL_SPAWNER
          );

        getOrCreateTagBuilder(DRAGON_EGG)
          .add(Blocks.DRAGON_EGG);

        FabricTagProvider<Block>.FabricTagBuilder unwaxedCopper = getOrCreateTagBuilder(UNWAXED_COPPER_BLOCKS);
        FabricTagProvider<Block>.FabricTagBuilder waxedCopper = getOrCreateTagBuilder(WAXED_COPPER_BLOCKS);
        for (Block block : Registries.BLOCK) {
            Identifier id = Registries.BLOCK.getId(block);
            boolean waxed = id.getPath().startsWith("waxed_");
            if (block instanceof Oxidizable) {
                unwaxedCopper.add(block);
            } else if (waxed) {
                waxedCopper.add(block);
            }
        }

        getOrCreateTagBuilder(COPPER_BLOCKS)
          .addTag(UNWAXED_COPPER_BLOCKS)
          .addTag(WAXED_COPPER_BLOCKS);

        getOrCreateTagBuilder(EXTINGUISHABLE)
          .forceAddTag(BlockTags.FIRE)
          .forceAddTag(BlockTags.CAMPFIRES)
          .forceAddTag(BlockTags.CANDLES)
          .forceAddTag(BlockTags.CANDLE_CAKES);

        getOrCreateTagBuilder(OTHERS)
          .forceAddTag(BlockTags.CANDLE_CAKES)
          .forceAddTag(BlockTags.CANDLES)
          .forceAddTag(BlockTags.BANNERS)
          .forceAddTag(BlockTags.BEDS)
          .add(
            Blocks.CAKE,
            Blocks.BELL
          );

        getOrCreateTagBuilder(HARVEST)
          .forceAddTag(BlockTags.CAVE_VINES)
          .add(
            Blocks.SWEET_BERRY_BUSH,
            Blocks.PUMPKIN
          );

        getOrCreateTagBuilder(SIGNS)
          .forceAddTag(BlockTags.SIGNS);
    }
}
