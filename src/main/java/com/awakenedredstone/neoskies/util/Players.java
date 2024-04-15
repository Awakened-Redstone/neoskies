package com.awakenedredstone.neoskies.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import com.awakenedredstone.neoskies.logic.Skylands;

import java.util.Optional;

public class Players {
    static final MinecraftServer server = Skylands.getServer();

    public static Optional<PlayerEntity> get(String name) {
        for (var player : server.getPlayerManager().getPlayerList()) {
            if (player.getName().getString().equals(name)) return Optional.of(player);
        }
        return Optional.empty();
    }
}
