package com.awakenedredstone.neoskies.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerEvents {
    Event<PlayerTick> TICK = EventFactory.createArrayBacked(PlayerTick.class, (listeners) -> player -> {
        for (PlayerTick event : listeners) {
            event.onPlayerTick(player);
        }
    });

    Event<PostPlayerDeath> POST_DEATH = EventFactory.createArrayBacked(PostPlayerDeath.class, (listeners) -> player -> {
        for (PostPlayerDeath event : listeners) {
            event.onPlayerPostDeath(player);
        }
    });

    @FunctionalInterface
    interface PlayerTick {
        void onPlayerTick(ServerPlayerEntity player);
    }

    @FunctionalInterface
    interface PostPlayerDeath {
        /**
         * Called before ticking the whole hud (while not paused), which is displayed in game, in a world.
         */
        void onPlayerPostDeath(ServerPlayerEntity player);
    }
}
