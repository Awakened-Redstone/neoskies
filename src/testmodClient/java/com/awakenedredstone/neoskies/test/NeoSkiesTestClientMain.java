package com.awakenedredstone.neoskies.test;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class NeoSkiesTestClientMain implements ClientModInitializer {
    private static int tick = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (tick++ % 2 == 0) {
                DebugImGuiRenderer.MEMORY.add((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("sbac")
                .then(ClientCommandManager.literal("debug")
                  .executes(context -> {
                      DebugImGuiRenderer.OPEN.set(true);
                      return 0;
                  })
                )
            );
        });
    }
}
