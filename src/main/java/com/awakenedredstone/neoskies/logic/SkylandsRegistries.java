package com.awakenedredstone.neoskies.logic;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import com.awakenedredstone.neoskies.SkylandsMain;
import com.awakenedredstone.neoskies.api.island.PermissionLevel;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;

public class SkylandsRegistries {
    public static final SimpleRegistry<IslandSettings> ISLAND_SETTINGS = createRegistry(SkylandsMain.id("island_settings"));
    public static final SimpleRegistry<PermissionLevel> PERMISSION_LEVELS = createRegistry(SkylandsMain.id("permission_levels"));

    @SuppressWarnings("unchecked")
    private static <T> SimpleRegistry<T> createRegistry(Identifier identifier) {
        return (SimpleRegistry<T>) FabricRegistryBuilder.createSimple(RegistryKey.ofRegistry(identifier)).buildAndRegister();
    }
}