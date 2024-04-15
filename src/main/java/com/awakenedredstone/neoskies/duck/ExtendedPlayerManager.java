package com.awakenedredstone.neoskies.duck;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ExtendedPlayerManager {
    ServerPlayerEntity resetPlayer(ServerPlayerEntity player);
}
