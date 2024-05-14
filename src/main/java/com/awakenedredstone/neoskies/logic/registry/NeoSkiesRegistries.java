package com.awakenedredstone.neoskies.logic.registry;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.api.island.PermissionLevel;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

public class NeoSkiesRegistries {
    public static final SimpleRegistry<IslandSettings> ISLAND_SETTINGS = createRegistry(NeoSkies.id("island_settings"));
    public static final SimpleRegistry<PermissionLevel> PERMISSION_LEVELS = createRegistry(NeoSkies.id("permission_levels"));

    @SuppressWarnings("unchecked")
    private static <T> SimpleRegistry<T> createRegistry(Identifier identifier) {
        return (SimpleRegistry<T>) FabricRegistryBuilder.createSimple(RegistryKey.ofRegistry(identifier)).buildAndRegister();
    }
}
