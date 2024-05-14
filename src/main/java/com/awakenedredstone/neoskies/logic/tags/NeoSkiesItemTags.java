package com.awakenedredstone.neoskies.logic.tags;

import com.awakenedredstone.neoskies.NeoSkies;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class NeoSkiesItemTags {
    public static final TagKey<Item> MINECART = TagKey.of(RegistryKeys.ITEM, NeoSkies.id("protection/use/minecart"));
    public static final TagKey<Item> LEAD = TagKey.of(RegistryKeys.ITEM, NeoSkies.id("protection/lead"));
    public static final TagKey<Item> PLACE = TagKey.of(RegistryKeys.ITEM, NeoSkies.id("protection/place"));
    public static final TagKey<Item> CONTAINERS = TagKey.of(RegistryKeys.ITEM, NeoSkies.id("protection/containers"));
    public static final TagKey<Item> LODESTONE = TagKey.of(RegistryKeys.ITEM, NeoSkies.id("protection/locestone"));
    public static final TagKey<Item> SPAWNER = TagKey.of(RegistryKeys.ITEM, NeoSkies.id("protection/spawner"));
}
