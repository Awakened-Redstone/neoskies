package com.awakenedredstone.neoskies.config;

import blue.endless.jankson.Comment;
import com.awakenedredstone.neoskies.config.source.Config;
import com.awakenedredstone.neoskies.config.source.JanksonBuilder;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class MainConfig extends Config {
    public MainConfig() {
        super("neoskies/config", JanksonBuilder.buildJankson());
    }

    @Comment("The mod language")
    public String language = "en_us";

    public Commands commands = new Commands();
    public IslandScan islandScan = new IslandScan();

    @Comment("Disables spawning lightning and horse traps in the hub")
    public boolean disableLightningOnHub = true;

    @Comment("Allow the player to visit their own island")
    public boolean allowVisitCurrentIsland = false;

    @Comment("The default island radius")
    public int defaultIslandRadius = -1;

    @Comment("The cooldown before the player can delete their island (in seconds)")
    public int deletionCooldown = -1;

    @Comment("The maximum amount of islands a player can create")
    public int islandLimit = -1;

    @Comment("Whenever it should clear the player data when they clear their island")
    public boolean resetPlayerWithIsland = false;

    @Comment("The default island location")
    public Vec3d defaultIslandLocation = new Vec3d(0.5d, 75d, 0.5d);

    @Comment("Disable blocks outside the islands")
    public boolean disableBlocksOutsideIslands = false;

    @Comment("Disable entities outside the islands")
    public boolean disableEntitiesOutsideIslands = false;

    @Comment("Enable end islands")
    public boolean enableEndIsland = false;

    @Comment("Whenever the player is protected from falling into the void")
    public boolean safeVoid = false;

    @Comment("Whenever the player is protected from fall damage when being recovered from the void")
    public boolean safeVoidFallDamage = true;

    @Comment("The amount of blocks the player is protected from falling into the void")
    public byte safeVoidBlocksBelow = 16;

    @Comment("Whenever the player gets the island protection messages")
    public boolean showProtectionMessages = true;

    public static class Commands {
        @Comment("The mod main command")
        public String command = "sb";

        @Comment("The mod main command aliases")
        public List<String> commandAliases = new ArrayList<>(List.of("skyblock"));

        @Comment("The mod admin command")
        public String adminCommand = "sba";

        @Comment("The mod admin command aliases")
        public List<String> adminCommandAliases = new ArrayList<>(List.of("skyblockadmin"));
    }

    public static class IslandScan {
        @Comment("The amount of cores dedicated for processing the chunk data on an island scan")
        public byte chunkCores = 4;
    }
}
