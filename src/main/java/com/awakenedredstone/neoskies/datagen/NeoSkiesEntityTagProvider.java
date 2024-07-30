package com.awakenedredstone.neoskies.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesEntityTags.ARMOR_STAND;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesEntityTags.BOATS;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesEntityTags.BREAK;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesEntityTags.ITEM_FRAME;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesEntityTags.LEASH_KNOT;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesEntityTags.MINECARTS;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesEntityTags.RIDEABLE;

public class NeoSkiesEntityTagProvider extends FabricTagProvider.EntityTypeTagProvider {
    public NeoSkiesEntityTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(ARMOR_STAND)
          .add(EntityType.ARMOR_STAND);

        getOrCreateTagBuilder(LEASH_KNOT)
          .add(EntityType.LEASH_KNOT);

        getOrCreateTagBuilder(ITEM_FRAME)
          .add(
            EntityType.ITEM_FRAME,
            EntityType.GLOW_ITEM_FRAME
          );

        getOrCreateTagBuilder(MINECARTS)
          .forceAddTag(ConventionalEntityTypeTags.MINECARTS);

        getOrCreateTagBuilder(BOATS)
          .forceAddTag(ConventionalEntityTypeTags.BOATS);

        getOrCreateTagBuilder(RIDEABLE)
          .add(
            EntityType.LLAMA,
            EntityType.TRADER_LLAMA,
            EntityType.HORSE,
            EntityType.ZOMBIE_HORSE,
            EntityType.SKELETON_HORSE,
            EntityType.MULE,
            EntityType.DONKEY,
            EntityType.PIG,
            EntityType.STRIDER,
            EntityType.CAMEL
          );

        getOrCreateTagBuilder(BREAK)
          .add(EntityType.CREEPER);
    }
}
