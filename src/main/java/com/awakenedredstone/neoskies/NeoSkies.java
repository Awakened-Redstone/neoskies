package com.awakenedredstone.neoskies;

/*
Credits:
 - Skylands, the mod this is based on, responsible for most of the island generation code
*/

import com.awakenedredstone.neoskies.command.NeoSkiesCommands;
import com.awakenedredstone.neoskies.font.FontManager;
import com.awakenedredstone.neoskies.logic.EventListeners;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.logic.protection.NeoSkiesProtectionProvider;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesPermissionLevels;
import com.awakenedredstone.neoskies.util.LinedStringBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.pb4.common.protection.api.CommonProtection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class NeoSkies implements ModInitializer {
    public static final String MOD_ID = "neoskies";
    public static final Logger LOGGER = LoggerFactory.getLogger("NeoSkies");
    public static final Set<PlayerEntity> PROTECTION_BYPASS = new HashSet<>();
    public static final Gson GSON = new GsonBuilder().setLenient().create();

    //TODO: Simple (and optimised) datapack based island templates
    //TODO: Fix "Loading terrain..." showing when moving between island and hub
    //TODO: Don't save empty chunks
    //TODO: Add scan cooldown, and disable it outside of the island.
    @Override
    public void onInitialize() {
        CommonProtection.register(NeoSkies.id("neoskies"), new NeoSkiesProtectionProvider());

        NeoSkiesIslandSettings.init();
        NeoSkiesPermissionLevels.init();
        EventListeners.registerEvents();
        NeoSkiesCommands.init();
        FontManager.init();

        IslandLogic.getConfig().load();
        IslandLogic.getRankingConfig().load();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LinedStringBuilder message = new LinedStringBuilder()
                .appendLine("  You are using an alpha build of NeoSkies, it may have bugs and performance issues!")
                .appendLine("  Feature will get added and changed in the future.")
                .appendLine("  Please report bugs and suggest changes at the project github page: https://github.com/Awakened-Redstone/neoskies/issues")
                .appendLine("  Discuss about the mod at the discord server: https://discord.gg/MTqsjwMpN2");
            LOGGER.warn("\n{}", message.toString());
        });
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
