package com.awakenedredstone.neoskies.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.MinecartItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesItemTags.CONTAINERS;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesItemTags.LEAD;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesItemTags.LODESTONE;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesItemTags.MINECART;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesItemTags.PLACE;
import static com.awakenedredstone.neoskies.logic.tags.NeoSkiesItemTags.SPAWNER;

public class NeoSkiesItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public NeoSkiesItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(PLACE)
          .forceAddTag(ItemTags.AXES)
          .forceAddTag(ItemTags.HOES)
          .forceAddTag(ItemTags.SHOVELS)
          .add(
            Items.ARMOR_STAND,
            Items.BONE_MEAL,
            Items.DEBUG_STICK,
            Items.ENDER_EYE,
            Items.FIRE_CHARGE,
            Items.HONEYCOMB,
            Items.SHEARS,
            Items.END_CRYSTAL,
            Items.FLINT_AND_STEEL,
            Items.POWDER_SNOW_BUCKET
          );

        var spawner = getOrCreateTagBuilder(SPAWNER);

        for (Item item : Registries.ITEM) {
            if (item instanceof SpawnEggItem) {
                spawner.add(item);
            }
        }

        getOrCreateTagBuilder(CONTAINERS)
          .add(
            Items.FILLED_MAP,
            Items.GLASS_BOTTLE
          );

        getOrCreateTagBuilder(LODESTONE)
          .add(Items.COMPASS);

        FabricTagProvider<Item>.FabricTagBuilder minecarts = getOrCreateTagBuilder(MINECART);
        for (Item item : Registries.ITEM) {
            if (item instanceof MinecartItem) {
                minecarts.add(item);
            }
        }

        getOrCreateTagBuilder(LEAD)
          .add(Items.LEAD);
    }
}
