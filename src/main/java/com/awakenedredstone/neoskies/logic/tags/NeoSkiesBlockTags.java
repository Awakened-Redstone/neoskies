package com.awakenedredstone.neoskies.logic.tags;

import com.awakenedredstone.neoskies.SkylandsMain;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class NeoSkiesBlockTags {
    public static final TagKey<Block> ANVIL = TagKey.of(RegistryKeys.BLOCK, SkylandsMain.id("islands/anvil"));
    public static final TagKey<Block> BEACON = TagKey.of(RegistryKeys.BLOCK, SkylandsMain.id("islands/beacon"));
    public static final TagKey<Block> BREWING_STAND = TagKey.of(RegistryKeys.BLOCK, SkylandsMain.id("islands/brewing_stand"));
    public static final TagKey<Block> COMPOSTER = TagKey.of(RegistryKeys.BLOCK, SkylandsMain.id("islands/composter"));
    public static final TagKey<Block> CONTAINERS = TagKey.of(RegistryKeys.BLOCK, SkylandsMain.id("islands/containers"));
    public static final TagKey<Block> DOORS = TagKey.of(RegistryKeys.BLOCK, SkylandsMain.id("islands/doors"));
    public static final TagKey<Block> LECTERN = TagKey.of(RegistryKeys.BLOCK, SkylandsMain.id("islands/lectern"));
    public static final TagKey<Block> LODESTONE = TagKey.of(RegistryKeys.BLOCK, SkylandsMain.id("islands/lodestone"));
    public static final TagKey<Block> REDSTONE = TagKey.of(RegistryKeys.BLOCK, SkylandsMain.id("islands/redstone"));
    public static final TagKey<Block> RESPAWN_ANCHOR = TagKey.of(RegistryKeys.BLOCK, SkylandsMain.id("islands/respawn_anchor"));
    //public static final TagKey<Block> DRIPLEAF = TagKey.of(RegistryKeys.BLOCK, SkylandsMain.id("islands/dripleaf"));
    //public static final TagKey<Block> SCULK = TagKey.of(RegistryKeys.BLOCK, SkylandsMain.id("islands/sculk"));
}
