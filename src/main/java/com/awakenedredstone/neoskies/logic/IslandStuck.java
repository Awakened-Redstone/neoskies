package com.awakenedredstone.neoskies.logic;

import com.awakenedredstone.neoskies.duck.ExtendedPlayerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class IslandStuck {
    public final ArrayList<Island> stuck = new ArrayList<>();

    public Island create(PlayerEntity player) {
        for (var island : this.stuck) {
            if (island.owner.uuid.equals(player.getUuid())) return island;
        }
        var island = new Island(player);
        island.freshCreated = true;
        this.stuck.add(island);
        return island;
    }

    public void delete(Island island) {
        MinecraftServer server = IslandLogic.getServer();

        island.getOverworldHandler().delete();
        island.getNetherHandler().delete();
        island.getEndHandler().delete();

        PlayerManager playerManager = server.getPlayerManager();

        island.members.forEach(member -> {
            ServerPlayerEntity player = playerManager.getPlayer(member.uuid);
            if (player != null) {
                player.closeHandledScreen();
            }
        });

        if (IslandLogic.getConfig().resetPlayerWithIsland) {
            Member owner = island.owner;
            deletePlayer(owner);

            island.members.forEach(this::deletePlayer);
        }

        island.getOverworld().getPlayers().forEach(player -> IslandLogic.runOnNextTick(() -> IslandLogic.getInstance().hub.visit(player, true)));
        island.getNether().getPlayers().forEach(player -> IslandLogic.runOnNextTick(() -> IslandLogic.getInstance().hub.visit(player, true)));
        island.getEnd().getPlayers().forEach(player -> IslandLogic.runOnNextTick(() -> IslandLogic.getInstance().hub.visit(player, true)));

        stuck.remove(island);
    }

    private void deletePlayer(Member member) {
        MinecraftServer server = IslandLogic.getServer();
        PlayerManager playerManager = server.getPlayerManager();
        Path playerDataDir = server.saveHandler.playerDataDir.toPath();

        playerDataDir.resolve(member.uuid.toString() + ".dat").toFile().delete();
        ServerPlayerEntity player = playerManager.getPlayer(member.uuid);
        if (player != null) {
            ServerPlayerEntity newPlayer = ((ExtendedPlayerManager) playerManager).resetPlayer(player);
            player.networkHandler.player = newPlayer;
            server.saveHandler.savePlayerData(newPlayer);
            IslandLogic.getInstance().hub.visit(newPlayer, true);
        }
    }

    public void delete(PlayerEntity player) {
        this.getByPlayer(player).ifPresent(this::delete);
    }

    public void delete(String playerName) {
        this.getByPlayer(playerName).ifPresent(this::delete);
    }

    public Optional<Island> getByPlayer(PlayerEntity player) {
        for (var island : this.stuck) {
            if (island.owner.uuid.equals(player.getUuid())) return Optional.of(island);
        }
        return Optional.empty();
    }

    public Optional<Island> getByPlayer(String playerName) {
        for (var island : this.stuck) {
            if (island.owner.name.equals(playerName)) return Optional.of(island);
        }
        return Optional.empty();
    }

    public Optional<Island> getByPlayer(UUID playerUuid) {
        for (var island : this.stuck) {
            if (island.owner.uuid.equals(playerUuid)) return Optional.of(island);
        }
        return Optional.empty();
    }

    public Optional<Island> getById(String islandId) {
        return getById(UUID.fromString(islandId));
    }

    public Optional<Island> getById(UUID islandId) {
        for (var island : this.stuck) {
            if (island.getIslandId().equals(islandId)) return Optional.of(island);
        }
        return Optional.empty();
    }

    public Optional<Island> get(UUID islandId) {
        for (var island : this.stuck) {
            if (island.getIslandId().equals(islandId)) return Optional.of(island);
        }
        return Optional.empty();
    }

    public Optional<Island> getFromMember(PlayerEntity player) {
        for (var island : this.stuck) {
            if (island.owner.uuid.equals(player.getUuid())) {
                return Optional.of(island);
            } else if (island.isMember(player)) {
                return Optional.of(island);
            }
        }
        return Optional.empty();
    }

    public boolean hasIsland(UUID uuid) {
        for (var island : this.stuck) {
            if (island.owner.uuid.equals(uuid)) return true;
        }
        return false;
    }

    /*
    TODO:
     Store island data on the island world (cardinal) component,
     only keeping required data on the main world (cardinal) component
    */
    public void readFromNbt(NbtCompound nbt) {
        NbtCompound islandStuckNbt = nbt.getCompound("islandStuck");
        int size = islandStuckNbt.getInt("size");
        for (int i = 0; i < size; i++) {
            NbtCompound islandNbt = islandStuckNbt.getCompound(String.valueOf(i));
            Island island = Island.fromNbt(islandNbt);
            if (!this.hasIsland(island.owner.uuid)) {
                this.stuck.add(island);
            }
        }
    }

    /*
    TODO:
     Store island data on the island world (cardinal) component,
     only keeping required data on the main world (cardinal) component
    */
    public void writeToNbt(NbtCompound nbt) {
        NbtCompound islandStuckNbt = new NbtCompound();
        islandStuckNbt.putInt("size", this.stuck.size());
        for (int i = 0; i < this.stuck.size(); i++) {
            Island island = this.stuck.get(i);
            islandStuckNbt.put(Integer.toString(i), island.toNbt());
        }
        nbt.put("islandStuck", islandStuckNbt);
    }
}
