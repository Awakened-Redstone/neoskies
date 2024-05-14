package com.awakenedredstone.neoskies.logic.tags;

import com.awakenedredstone.neoskies.NeoSkies;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class NeoSkiesEntityTags {
    public static final TagKey<EntityType<?>> ARMOR_STAND = TagKey.of(RegistryKeys.ENTITY_TYPE, NeoSkies.id("protection/interact/armor_stand"));
    public static final TagKey<EntityType<?>> LEASH_KNOT = TagKey.of(RegistryKeys.ENTITY_TYPE, NeoSkies.id("protection/use/leash_knot"));
    public static final TagKey<EntityType<?>> ITEM_FRAME = TagKey.of(RegistryKeys.ENTITY_TYPE, NeoSkies.id("protection/use/item_frame"));
    public static final TagKey<EntityType<?>> MINECARTS = TagKey.of(RegistryKeys.ENTITY_TYPE, NeoSkies.id("protection/ride/minecarts"));
    public static final TagKey<EntityType<?>> BOATS = TagKey.of(RegistryKeys.ENTITY_TYPE, NeoSkies.id("protection/ride/item_frame"));
    public static final TagKey<EntityType<?>> RIDEABLE = TagKey.of(RegistryKeys.ENTITY_TYPE, NeoSkies.id("protection/ride/rideable"));
    public static final TagKey<EntityType<?>> BREAK = TagKey.of(RegistryKeys.ENTITY_TYPE, NeoSkies.id("protection/break/block"));
}
