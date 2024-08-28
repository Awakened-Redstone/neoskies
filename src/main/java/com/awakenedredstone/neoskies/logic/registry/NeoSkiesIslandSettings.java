package com.awakenedredstone.neoskies.logic.registry;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags;
import com.awakenedredstone.neoskies.logic.tags.NeoSkiesEntityTags;
import com.awakenedredstone.neoskies.logic.tags.NeoSkiesItemTags;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class NeoSkiesIslandSettings {
    protected static final Map<TagKey<Block>, IslandSettings> RULE_BLOCK_USE_TAG = new HashMap<>();
    protected static final Map<TagKey<Block>, IslandSettings> RULE_BLOCK_USE_WITH_ITEM_TAG = new HashMap<>();
    protected static final Map<TagKey<EntityType<?>>, IslandSettings> RULE_ENTITY_TAG = new HashMap<>();
    protected static final Map<TagKey<Item>, IslandSettings> RULE_ITEM_TAG = new HashMap<>();

    public static final IslandSettings PLACE_BLOCKS = register("place/blocks", Items.STONE);
    public static final IslandSettings PLACE_MINECARTS = register("place/minecarts", Items.MINECART);
    public static final IslandSettings BREAK_BLOCKS = register("break/blocks", Items.WOODEN_PICKAXE);

    public static final IslandSettings USE_ANVIL = register("use/anvil", Items.ANVIL);
    public static final IslandSettings USE_ARMOR_STAND = register("use/armor_stand", Items.ARMOR_STAND);
    public static final IslandSettings USE_BEACON = register("use/beacon", Items.BEACON);
    public static final IslandSettings USE_BREWING_STAND = register("use/brewing_stand", Items.BREWING_STAND);
    public static final IslandSettings USE_COMPOSTER = register("use/composter", Items.COMPOSTER);
    public static final IslandSettings USE_CONTAINERS = register("use/containers", Items.CHEST);
    public static final IslandSettings USE_DOORS = register("use/doors", Items.OAK_DOOR);
    public static final IslandSettings USE_ITEM_FRAME = register("use/item_frame", Items.ITEM_FRAME);
    public static final IslandSettings USE_LECTERN = register("use/lectern", Items.LECTERN);
    public static final IslandSettings USE_LODESTONE = register("use/lodestone", Items.LODESTONE);
    public static final IslandSettings USE_REDSTONE = register("use/redstone", Items.REDSTONE);
    public static final IslandSettings USE_RESPAWN_ANCHOR = register("use/respawn_anchor", Items.RESPAWN_ANCHOR);
    public static final IslandSettings USE_SIGNS = register("use/signs", Items.OAK_SIGN);
    public static final IslandSettings USE_SPAWNER = register("use/spawner", Items.SPAWNER);
    public static final IslandSettings USE_TNT = register("use/tnt", Items.TNT);

    public static final IslandSettings INTERACT_DRIPLEAF = register("interact/dripleaf", Items.BIG_DRIPLEAF);
    public static final IslandSettings INTERACT_DRAGON_EGG = register("interact/dragon_egg", Items.DRAGON_EGG);
    public static final IslandSettings INTERACT_SCULK = register("interact/sculk", Items.SCULK);
    public static final IslandSettings INTERACT_OTHER_BLOCKS = register("interact/other_blocks", Items.BELL);

    public static final IslandSettings HARVEST = register("interact/harvest", Items.SWEET_BERRIES);

    public static final IslandSettings RIDE_MINECARTS = register("ride/minecarts", Items.MINECART);
    public static final IslandSettings RIDE_BOATS = register("ride/boats", Items.OAK_BOAT);
    public static final IslandSettings RIDE_OTHERS = register("ride/others", Items.SADDLE);
    public static final IslandSettings LEASH_ENTITY = register("leash/entity", Items.LEAD);
    public static final IslandSettings SHEAR_ENTITY = register("shear/entity", Items.SHEARS);

    public static final IslandSettings HURT_HOSTILE = register("hurt/hostile", Items.DIAMOND_SWORD);
    public static final IslandSettings HURT_PASSIVE = register("hurt/passive", Items.WOODEN_SWORD);
    public static final IslandSettings BUCKET_PASSIVE = register("bucket/passive", Items.AXOLOTL_BUCKET);

    static {
        addBlockUseTag(NeoSkiesBlockTags.ANVIL, USE_ANVIL);
        addBlockUseTag(NeoSkiesBlockTags.BEACON, USE_BEACON);
        addBlockUseTag(NeoSkiesBlockTags.BREWING_STAND, USE_BREWING_STAND);
        addBlockUseTag(NeoSkiesBlockTags.COMPOSTER, USE_COMPOSTER);
        addBlockUseTag(NeoSkiesBlockTags.CONTAINERS, USE_CONTAINERS);
        addBlockUseWithItemTag(NeoSkiesBlockTags.CONTAINERS_WITH_ITEM, USE_CONTAINERS);
        addBlockUseTag(NeoSkiesBlockTags.DOORS, USE_DOORS);
        addBlockUseTag(NeoSkiesBlockTags.LECTERN, USE_LECTERN);
        addBlockUseTag(NeoSkiesBlockTags.LODESTONE, USE_LODESTONE);
        addBlockUseTag(NeoSkiesBlockTags.OTHERS, INTERACT_OTHER_BLOCKS);
        addBlockUseTag(NeoSkiesBlockTags.REDSTONE, USE_REDSTONE);
        addBlockUseTag(NeoSkiesBlockTags.RESPAWN_ANCHOR, USE_RESPAWN_ANCHOR);
        addBlockUseTag(NeoSkiesBlockTags.SPAWNER, USE_SPAWNER);
        addBlockUseTag(NeoSkiesBlockTags.SIGNS, USE_SIGNS);
        addBlockUseTag(NeoSkiesBlockTags.DRAGON_EGG, INTERACT_DRAGON_EGG);
        addBlockUseTag(NeoSkiesBlockTags.HARVEST, HARVEST);

        addItemTag(NeoSkiesItemTags.PLACE, PLACE_BLOCKS);
        addItemTag(NeoSkiesItemTags.MINECART, PLACE_MINECARTS);
        addItemTag(NeoSkiesItemTags.CONTAINERS, USE_CONTAINERS);
        addItemTag(NeoSkiesItemTags.LODESTONE, USE_LODESTONE);
        addItemTag(NeoSkiesItemTags.SPAWNER, USE_SPAWNER);

        addEntityTag(NeoSkiesEntityTags.BREAK, BREAK_BLOCKS);
        addEntityTag(NeoSkiesEntityTags.MINECARTS, RIDE_MINECARTS);
        addEntityTag(NeoSkiesEntityTags.BOATS, RIDE_BOATS);
        addEntityTag(NeoSkiesEntityTags.RIDEABLE, RIDE_OTHERS);
        addEntityTag(NeoSkiesEntityTags.ARMOR_STAND, USE_ARMOR_STAND);
        addEntityTag(NeoSkiesEntityTags.ITEM_FRAME, USE_ITEM_FRAME);
        addEntityTag(NeoSkiesEntityTags.LEASH_KNOT, LEASH_ENTITY);
    }

    public static void addBlockUseTag(TagKey<Block> tagKey, IslandSettings settings) {
        RULE_BLOCK_USE_TAG.put(tagKey, settings);
    }

    public static void addBlockUseWithItemTag(TagKey<Block> tagKey, IslandSettings settings) {
        RULE_BLOCK_USE_WITH_ITEM_TAG.put(tagKey, settings);
    }

    public static void addEntityTag(TagKey<EntityType<?>> tagKey, IslandSettings settings) {
        RULE_ENTITY_TAG.put(tagKey, settings);
    }

    public static void addItemTag(TagKey<Item> tagKey, IslandSettings settings) {
        RULE_ITEM_TAG.put(tagKey, settings);
    }

    public static Map<TagKey<Block>, IslandSettings> getRuleBlockTags() {
        return RULE_BLOCK_USE_TAG;
    }

    public static Map<TagKey<Block>, IslandSettings> getRuleBlockUseTag() {
        return RULE_BLOCK_USE_TAG;
    }

    public static Map<TagKey<EntityType<?>>, IslandSettings> getRuleEntityTags() {
        return RULE_ENTITY_TAG;
    }

    public static Map<TagKey<Item>, IslandSettings> getRuleItemTags() {
        return RULE_ITEM_TAG;
    }

    private static IslandSettings register(String id, Item icon) {
        return new IslandSettings(NeoSkies.id(id), icon);
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
                    Registry.register(NeoSkiesRegistries.ISLAND_SETTINGS, settings.getIdentifier(), settings);
                }

                if (!access) field.setAccessible(false);
            } catch (IllegalAccessException e) {
                NeoSkies.LOGGER.error("Failed to register island settings", e);
            }
        }
    }
}
