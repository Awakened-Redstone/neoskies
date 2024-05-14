package com.awakenedredstone.neoskies.data;

import com.awakenedredstone.neoskies.SkylandsMain;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

public class SkylandComponents implements WorldComponentInitializer/*, EntityComponentInitializer*/ {
    public static final ComponentKey<WorldComponent> WORLD_DATA = ComponentRegistryV3.INSTANCE.getOrCreate(SkylandsMain.id("world_data"), WorldComponent.class);
    //public static final ComponentKey<PlayerComponent> PLAYER_DATA = ComponentRegistryV3.INSTANCE.getOrCreate(SkylandsMain.id("player_data"), PlayerComponent.class);

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(WORLD_DATA, WorldComponent::new);
    }

    /*@Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PLAYER_DATA, PlayerComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }*/
}
