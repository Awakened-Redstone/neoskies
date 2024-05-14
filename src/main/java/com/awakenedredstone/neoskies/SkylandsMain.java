package com.awakenedredstone.neoskies;

import com.awakenedredstone.neoskies.command.SkylandsCommands;
import com.awakenedredstone.neoskies.font.FontManager;
import com.awakenedredstone.neoskies.logic.Skylands;
import com.awakenedredstone.neoskies.logic.SkylandsEventListeners;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.registry.SkylandsPermissionLevels;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class SkylandsMain implements ModInitializer {
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
        SkylandsPermissionLevels.init();
        SkylandsEventListeners.registerEvents();
        SkylandsCommands.init();
        FontManager.init();

        Skylands.getConfig().load();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
