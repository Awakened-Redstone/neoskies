package com.awakenedredstone.neoskies;

/*
Credits:
 - Skylands, the mod this is based on, responsible for most of the island generation code
*/

import com.awakenedredstone.neoskies.command.NeoSkiesCommands;
import com.awakenedredstone.neoskies.font.FontManager;
import com.awakenedredstone.neoskies.logic.EventListeners;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesPermissionLevels;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
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

    //TODO: Add GUIs
    //TODO: Better config system
    //TODO: Simple (and optimised) datapack based island templates
    //TODO: Refactor the entire mod for cleaner and better code
    @Override
    public void onInitialize() {
        NeoSkiesIslandSettings.init();
        NeoSkiesPermissionLevels.init();
        EventListeners.registerEvents();
        NeoSkiesCommands.init();
        FontManager.init();

        IslandLogic.getConfig().load();
        IslandLogic.getRankingConfig().load();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
