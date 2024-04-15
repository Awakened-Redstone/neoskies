package com.awakenedredstone.neoskies.logic.registry;

import com.awakenedredstone.neoskies.SkylandsMain;
import com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags;
import com.awakenedredstone.neoskies.logic.settings.DefaultNeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.tags.NeoSkiesEntityTags;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import com.awakenedredstone.neoskies.logic.SkylandsRegistries;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import net.minecraft.registry.tag.TagKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class NeoSkiesIslandSettings {
    protected static final Map<TagKey<Block>, IslandSettings> RULE_BLOCK_TAG = new HashMap<>();
    protected static final Map<TagKey<EntityType<?>>, IslandSettings> RULE_ENTITY_TAG = new HashMap<>();

    public static final IslandSettings PLACE_BLOCKS = new DefaultNeoSkiesIslandSettings("place_blocks", Items.STONE);
    public static final IslandSettings BREAK_BLOCKS = new DefaultNeoSkiesIslandSettings("break_blocks", Items.WOODEN_PICKAXE);

    public static final IslandSettings USE_ANVIL = new DefaultNeoSkiesIslandSettings("use_anvil", Items.ANVIL);
    public static final IslandSettings USE_BEACON = new DefaultNeoSkiesIslandSettings("use_beacon", Items.BEACON);
    public static final IslandSettings USE_BREWING_STAND = new DefaultNeoSkiesIslandSettings("use_brewing_stand", Items.BREWING_STAND);
    public static final IslandSettings USE_COMPOSTER = new DefaultNeoSkiesIslandSettings("use_composter", Items.COMPOSTER);
    public static final IslandSettings USE_CONTAINERS = new DefaultNeoSkiesIslandSettings("use_containers", Items.CHEST);
    public static final IslandSettings USE_DOORS = new DefaultNeoSkiesIslandSettings("use_doors", Items.OAK_DOOR);
    public static final IslandSettings USE_LECTERN = new DefaultNeoSkiesIslandSettings("use_lectern", Items.LECTERN);
    public static final IslandSettings USE_LODESTONE = new DefaultNeoSkiesIslandSettings("use_lodestone", Items.LODESTONE);
    public static final IslandSettings USE_REDSTONE = new DefaultNeoSkiesIslandSettings("use_redstone", Items.REDSTONE);
    public static final IslandSettings USE_RESPAWN_ANCHOR = new DefaultNeoSkiesIslandSettings("use_respawn_anchor", Items.RESPAWN_ANCHOR);
    public static final IslandSettings INTERACT_DRIPLEAF = new DefaultNeoSkiesIslandSettings("interact_dripleaf", Items.BIG_DRIPLEAF);
    public static final IslandSettings INTERACT_SCULK = new DefaultNeoSkiesIslandSettings("interact_sculk", Items.SCULK);
    public static final IslandSettings INTERACT_OTHER_BLOCKS = new DefaultNeoSkiesIslandSettings("interact_other_blocks", Items.BELL, true);

    public static final IslandSettings USE_ARMOR_STAND = new DefaultNeoSkiesIslandSettings("use_armor_stand", Items.ARMOR_STAND);
    public static final IslandSettings LEASH_ENTITY = new DefaultNeoSkiesIslandSettings("leash_entity", Items.LEAD);
    public static final IslandSettings SHEAR_ENTITY = new DefaultNeoSkiesIslandSettings("shear_entity", Items.SHEARS);
    public static final IslandSettings HURT_HOSTILE = new DefaultNeoSkiesIslandSettings("hurt_hostile", Items.DIAMOND_SWORD);
    public static final IslandSettings HURT_PASSIVE = new DefaultNeoSkiesIslandSettings("hurt_passive", Items.WOODEN_SWORD);

    static {
        addBlockTag(NeoSkiesBlockTags.ANVIL, USE_ANVIL);
        addBlockTag(NeoSkiesBlockTags.BEACON, USE_BEACON);
        addBlockTag(NeoSkiesBlockTags.BREWING_STAND, USE_BREWING_STAND);
        addBlockTag(NeoSkiesBlockTags.COMPOSTER, USE_COMPOSTER);
        addBlockTag(NeoSkiesBlockTags.CONTAINERS, USE_CONTAINERS);
        addBlockTag(NeoSkiesBlockTags.DOORS, USE_DOORS);
        addBlockTag(NeoSkiesBlockTags.LECTERN, USE_LECTERN);
        addBlockTag(NeoSkiesBlockTags.LODESTONE, USE_LODESTONE);
        addBlockTag(NeoSkiesBlockTags.REDSTONE, USE_REDSTONE);
        addBlockTag(NeoSkiesBlockTags.RESPAWN_ANCHOR, USE_RESPAWN_ANCHOR);

        addEntityTag(NeoSkiesEntityTags.ARMOR_STAND, USE_ARMOR_STAND);
        addEntityTag(NeoSkiesEntityTags.LEASH_KNOT, LEASH_ENTITY);
    }

    public static void addBlockTag(TagKey<Block> tagKey, IslandSettings settings) {
        RULE_BLOCK_TAG.put(tagKey, settings);
    }

    public static void addEntityTag(TagKey<EntityType<?>> tagKey, IslandSettings settings) {
        RULE_ENTITY_TAG.put(tagKey, settings);
    }

    public static Map<TagKey<Block>, IslandSettings> getRuleBlockTags() {
        return RULE_BLOCK_TAG;
    }

    public static Map<TagKey<EntityType<?>>, IslandSettings> getRuleEntityTags() {
        return RULE_ENTITY_TAG;
    }

    public static void init() {
        Class<NeoSkiesIslandSettings> clazz = NeoSkiesIslandSettings.class;
        IslandSettings settings;
        for (Field field : clazz.getDeclaredFields()) {
            try {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers)) continue;
                boolean access = field.canAccess(null);
                if (!access) field.setAccessible(true);

                if (field.getType() == IslandSettings.class) {
                    settings = (IslandSettings) field.get(null);
                    Registry.register(SkylandsRegistries.ISLAND_SETTINGS, settings.getIdentifier(), settings);
                }

                if (!access) field.setAccessible(false);
            } catch (IllegalAccessException e) {
                SkylandsMain.LOGGER.error("Failed to register island settings", e);
            }
        }
    }
}
