package com.awakenedredstone.neoskies.logic.tags;

import com.awakenedredstone.neoskies.SkylandsMain;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class NeoSkiesItemTags {
    public static final TagKey<Item> BLOCK_INTERACTION = TagKey.of(RegistryKeys.ITEM, SkylandsMain.id("islands/block_interaction"));
    public static final TagKey<Item> GENERAL_INTERACTION = TagKey.of(RegistryKeys.ITEM, SkylandsMain.id("islands/general_interaction"));
}
