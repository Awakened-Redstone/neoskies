package skylands.logic.registry;

import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import skylands.logic.SkylandsRegistries;
import skylands.logic.settings.DefaultSkylandsIslandSettings;
import skylands.logic.settings.IslandSettings;

public class SkylandsIslandSettings implements AutoRegistryContainer<IslandSettings> {
    public static final IslandSettings PLACE = new DefaultSkylandsIslandSettings("place", Items.STONE);
    public static final IslandSettings BREAK = new DefaultSkylandsIslandSettings("break", Items.WOODEN_PICKAXE);
    public static final IslandSettings REDSTONE = new DefaultSkylandsIslandSettings("redstone", Items.REDSTONE);
    public static final IslandSettings BEACON = new DefaultSkylandsIslandSettings("beacon", Items.BEACON);
    public static final IslandSettings COMPOSTER = new DefaultSkylandsIslandSettings("composter", Items.COMPOSTER);
    public static final IslandSettings LODESTONE = new DefaultSkylandsIslandSettings("lodestone", Items.LODESTONE);
    public static final IslandSettings ANVIL = new DefaultSkylandsIslandSettings("anvil", Items.ANVIL);
    public static final IslandSettings BREWING_STAND = new DefaultSkylandsIslandSettings("brewing_stand", Items.BREWING_STAND);
    public static final IslandSettings CONTAINERS = new DefaultSkylandsIslandSettings("containers", Items.CHEST);
    public static final IslandSettings RESPAWN_ANCHOR = new DefaultSkylandsIslandSettings("respawn_anchor", Items.RESPAWN_ANCHOR);
    public static final IslandSettings HURT_HOSTILE = new DefaultSkylandsIslandSettings("hurt_hostile", Items.DIAMOND_SWORD);
    public static final IslandSettings HURT_PASSIVE = new DefaultSkylandsIslandSettings("hurt_passive", Items.WOODEN_SWORD);
    public static final IslandSettings DRIPLEAF = new DefaultSkylandsIslandSettings("dripleaf", Items.BIG_DRIPLEAF);

    @Override
    public Registry<IslandSettings> getRegistry() {
        return SkylandsRegistries.ISLAND_SETTINGS;
    }

    @Override
    public Class<IslandSettings> getTargetFieldType() {
        return IslandSettings.class;
    }
}
