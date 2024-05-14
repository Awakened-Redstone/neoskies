package com.awakenedredstone.neoskies.logic.tags;

import com.awakenedredstone.neoskies.NeoSkies;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class NeoSkiesBlockTags {
    public static final TagKey<Block> ANVIL = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/anvil"));
    public static final TagKey<Block> BEACON = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/beacon"));
    public static final TagKey<Block> BREWING_STAND = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/brewing_stand"));
    public static final TagKey<Block> COMPOSTER = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/composter"));
    public static final TagKey<Block> CONTAINERS = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/containers"));
    public static final TagKey<Block> CONTAINERS_WITH_ITEM = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/containers_with_item"));
    public static final TagKey<Block> DOORS = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/doors"));
    public static final TagKey<Block> DRAGON_EGG = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/interact/dragon_egg"));
    public static final TagKey<Block> EXTINGUISHABLE = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/extinguishable"));
    public static final TagKey<Block> HARVEST = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/harvest"));
    public static final TagKey<Block> LECTERN = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/lectern"));
    public static final TagKey<Block> LODESTONE = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/lodestone"));
    public static final TagKey<Block> OTHERS = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/interact/others"));
    public static final TagKey<Block> REDSTONE = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/redstone"));
    public static final TagKey<Block> RESPAWN_ANCHOR = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/respawn_anchor"));
    public static final TagKey<Block> SPAWNER = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/spawner"));
    public static final TagKey<Block> SIGNS = TagKey.of(RegistryKeys.BLOCK, NeoSkies.id("protection/use/signs"));

    public static final TagKey<Block> UNWAXED_COPPER_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier("c", "copper_blocks/unwaxed"));
    public static final TagKey<Block> WAXED_COPPER_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier("c", "copper_blocks/waxed"));
    public static final TagKey<Block> COPPER_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier("c", "copper_blocks"));
}
