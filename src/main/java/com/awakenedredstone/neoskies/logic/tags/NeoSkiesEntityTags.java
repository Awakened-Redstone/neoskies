package com.awakenedredstone.neoskies.logic.tags;

import com.awakenedredstone.neoskies.SkylandsMain;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class NeoSkiesEntityTags {
    public static final TagKey<EntityType<?>> ARMOR_STAND = TagKey.of(RegistryKeys.ENTITY_TYPE, SkylandsMain.id("islands/armor_stand"));
    public static final TagKey<EntityType<?>> LEASH_KNOT = TagKey.of(RegistryKeys.ENTITY_TYPE, SkylandsMain.id("islands/leash_knot"));
}
