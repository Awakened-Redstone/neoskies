package com.awakenedredstone.neoskies.api.events;

import com.awakenedredstone.neoskies.logic.Island;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public interface IslandEvents {
    Event<HubVisitEvent> ON_HUB_VISIT = EventFactory.createArrayBacked(HubVisitEvent.class, callbacks -> (player, world) -> {
        for (HubVisitEvent callback : callbacks) {
            callback.invoke(player, world);
        }
    });

    Event<GenericIslandEvent> ON_ISLAND_VISIT = EventFactory.createArrayBacked(GenericIslandEvent.class, callbacks -> (player, world, island) -> {
        for (GenericIslandEvent callback : callbacks) {
            callback.invoke(player, world, island);
        }
    });

    Event<GenericIslandEvent> ON_ISLAND_FIRST_LOAD = EventFactory.createArrayBacked(GenericIslandEvent.class, callbacks -> (player, world, island) -> {
        for (GenericIslandEvent callback : callbacks) {
            callback.invoke(player, world, island);
        }
    });

    Event<IslandFirstLoad> ON_NETHER_FIRST_LOAD = EventFactory.createArrayBacked(IslandFirstLoad.class, callbacks -> (world, island) -> {
        for (IslandFirstLoad callback : callbacks) {
            callback.onLoad(world, island);
        }
    });

    Event<IslandFirstLoad> ON_END_FIRST_LOAD = EventFactory.createArrayBacked(IslandFirstLoad.class, callbacks -> (world, island) -> {
        for (IslandFirstLoad callback : callbacks) {
            callback.onLoad(world, island);
        }
    });

    @FunctionalInterface
    interface GenericIslandEvent {
        void invoke(PlayerEntity player, World world, Island island);
    }

    @FunctionalInterface
    interface HubVisitEvent {
        void invoke(PlayerEntity player, World world);
    }

    @FunctionalInterface
    interface IslandFirstLoad {
        void onLoad(World world, Island island);
    }
}
