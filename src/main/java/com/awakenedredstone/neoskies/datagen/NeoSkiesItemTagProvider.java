package com.awakenedredstone.neoskies.datagen;

import com.awakenedredstone.neoskies.logic.tags.NeoSkiesItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class NeoSkiesItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public NeoSkiesItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        FabricTagProvider<Item>.FabricTagBuilder blockInteraction = getOrCreateTagBuilder(NeoSkiesItemTags.BLOCK_INTERACTION)
          .forceAddTag(ItemTags.AXES)
          .forceAddTag(ItemTags.HOES)
          .forceAddTag(ItemTags.SHOVELS)
          .forceAddTag(ItemTags.MUSIC_DISCS)
          .add(
            Items.ARMOR_STAND,
            Items.BONE_MEAL,
            Items.BRUSH,
            Items.COMPASS,
            Items.DEBUG_STICK,
            Items.END_CRYSTAL,
            Items.ENDER_EYE,
            Items.FILLED_MAP,
            Items.FIRE_CHARGE,
            Items.FIREWORK_ROCKET,
            Items.FLINT_AND_STEEL,
            Items.HONEYCOMB,
            Items.LEAD,
            Items.POWDER_SNOW_BUCKET,
            Items.SHEARS
          );

        for (Item item : Registries.ITEM) {
            if (item instanceof DecorationItem ||
              item instanceof MinecartItem ||
              item instanceof PlaceableOnWaterItem ||
              item instanceof SpawnEggItem ||
              item instanceof PotionItem
            ) {
                blockInteraction.add(item);
            }
        }

        FabricTagProvider<Item>.FabricTagBuilder generalInteraction = getOrCreateTagBuilder(NeoSkiesItemTags.BLOCK_INTERACTION)
          .forceAddTag(ItemTags.BOATS)
          .add(
            Items.ENDER_EYE,
            Items.GLASS_BOTTLE
          );

        for (Item item : Registries.ITEM) {
            if (item instanceof BucketItem ||
              item instanceof SpawnEggItem
            ) {
                generalInteraction.add(item);
            }
        }


    }
}
