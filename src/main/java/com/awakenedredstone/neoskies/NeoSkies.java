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
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesRegister;
import com.awakenedredstone.neoskies.util.LinedStringBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        NeoSkiesRegister.init();
        NeoSkiesIslandSettings.init();
        NeoSkiesPermissionLevels.init();
        EventListeners.listenForEvents();
        NeoSkiesCommands.init();
        FontManager.init();

        IslandLogic.getConfig().load();
        IslandLogic.getRankingConfig().load();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.warn("""

                You are using an alpha build of NeoSkies, it may have bugs and performance issues!
                Feature WILL be added, removed and changed!
                Please report bugs and suggest changes at the project github page: https://github.com/Awakened-Redstone/neoskies/issues
                Discuss about the mod in the discord server: https://discord.gg/MTqsjwMpN2

                Support the mod development at https://ko-fi.com/awakenedredstone
                Get 25% off on your first month on a game server at BisectHosting with code Redstone
                  https://bisecthosting.com/Redstone?r=neoskies_log
              """);
        });
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static Identifier generatedId(String path) {
        return new Identifier(MOD_ID + "_generated", path);
    }
}
