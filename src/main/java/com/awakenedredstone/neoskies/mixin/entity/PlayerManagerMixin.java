package com.awakenedredstone.neoskies.mixin.entity;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.duck.ExtendedPlayerManager;
import com.awakenedredstone.neoskies.duck.ExtendedServerPlayerEntity;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin implements ExtendedPlayerManager {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private List<ServerPlayerEntity> players;

    @Shadow
    public abstract void sendWorldInfo(ServerPlayerEntity player, ServerWorld world);

    @Shadow
    public abstract void sendCommandTree(ServerPlayerEntity player);

    @Shadow
    @Final
    private Map<UUID, ServerPlayerEntity> playerMap;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;", shift = At.Shift.BEFORE))
    private void loadIsland(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci, @Local RegistryKey<World> registryKey) {
        if (NeoSkiesAPI.isIsland(registryKey)) {
            Optional<Island> islandOptional = NeoSkiesAPI.getOptionalIsland(registryKey);
            if (islandOptional.isPresent()) {
                Island island = islandOptional.get();

                //Load the proper island dimension
                if (NeoSkiesAPI.isOverworld(registryKey)) {
                    LOGGER.debug("Loading overworld for {}", registryKey.getValue());
                    island.getOverworld();
                } else if (NeoSkiesAPI.isNether(registryKey)) {
                    LOGGER.debug("Loading nether for {}", registryKey.getValue());
                    island.getNether();
                } else if (NeoSkiesAPI.isEnd(registryKey)) {
                    LOGGER.debug("Loading end for {}", registryKey.getValue());
                    island.getEnd();
                }
            } else {
                LOGGER.warn("Unknown island {}, defaulting to hub", registryKey.getValue());
                IslandLogic.getInstance().hub.positionInto(player);
            }
        }
    }

    @ModifyExpressionValue(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getSpawnPointPosition()Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos neoskies$respawnOnIsland(BlockPos original, ServerPlayerEntity player) {
        if (NeoSkiesAPI.isIsland(player.getWorld())) {
            Optional<Island> islandOptional = NeoSkiesAPI.getOptionalIsland(player.getWorld());
            if (islandOptional.isPresent()) {
                Island island = islandOptional.get();
                if (island.isMember(player)) {
                    return BlockPos.ofFloored(island.spawnPos);
                } else {
                    return BlockPos.ofFloored(island.visitsPos);
                }
            }
        }
        return BlockPos.ofFloored(IslandLogic.getInstance().hub.pos);
    }

    @ModifyExpressionValue(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getSpawnPointDimension()Lnet/minecraft/registry/RegistryKey;"))
    private RegistryKey<World> neoskies$fixRespawnDimension(RegistryKey<World> original, ServerPlayerEntity player) {
        World playerWorld = player.getWorld();
        if (NeoSkiesAPI.isIsland(playerWorld)) {
            return NeoSkiesAPI.getOptionalIsland(playerWorld).get().getOverworldKey();
        }
        return IslandLogic.getServer().getOverworld().getRegistryKey();
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;findRespawnPosition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;FZZ)Ljava/util/Optional;"))
    private Optional<Vec3d> neoskies$respawnOnIsland(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive) {
        return Optional.of(pos.toCenterPos());
    }

    @Override
    //TODO: Update to newer version
    public ServerPlayerEntity resetPlayer(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new DeathMessageS2CPacket(player.getId(), ScreenTexts.EMPTY));
        player.getInventory().clear();
        player.wakeUp();
        player.refreshPosition();
        player.dropShoulderEntities();
        player.forgiveMobAnger();
        player.extinguish();
        player.setFrozenTicks(0);
        player.setOnFire(false);
        player.getDamageTracker().update();
        player.server.getAdvancementLoader().getAdvancements().forEach((advancement) -> {
            AdvancementProgress advancementProgress = player.getAdvancementTracker().getProgress(advancement);
            if (!advancementProgress.isAnyObtained()) return;
            for (String string : advancementProgress.getObtainedCriteria()) {
                player.getAdvancementTracker().revokeCriterion(advancement, string);
            }
        });
        player.getWorld().sendEntityStatus(player, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);

        this.players.remove(player);
        player.getServerWorld().removePlayer(player, Entity.RemovalReason.DISCARDED);
        BlockPos blockPos = BlockPos.ofFloored(IslandLogic.getInstance().hub.pos);
        float f = player.getSpawnAngle();
        boolean bl = player.isSpawnForced();
        ServerWorld serverWorld = this.server.getOverworld();
        ServerPlayerEntity serverPlayerEntity = new ServerPlayerEntity(this.server, serverWorld, player.getGameProfile(), player.getClientOptions());
        serverPlayerEntity.networkHandler = player.networkHandler;
        ((ExtendedServerPlayerEntity) serverPlayerEntity).neoskies$simpleCopyFrom(player);
        serverPlayerEntity.setId(player.getId());
        serverPlayerEntity.setMainArm(player.getMainArm());
        for (String string : player.getCommandTags()) {
            serverPlayerEntity.addCommandTag(string);
        }

        Vec3d vec3d = blockPos.toCenterPos();

        serverPlayerEntity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, f, 0.0f);
        serverPlayerEntity.setSpawnPoint(serverWorld.getRegistryKey(), blockPos, f, bl, false);

        while (!serverWorld.isSpaceEmpty(serverPlayerEntity) && serverPlayerEntity.getY() < (double) serverWorld.getTopY()) {
            serverPlayerEntity.setPosition(serverPlayerEntity.getX(), serverPlayerEntity.getY() + 1.0, serverPlayerEntity.getZ());
        }

        WorldProperties worldProperties = serverPlayerEntity.getWorld().getLevelProperties();
        serverPlayerEntity.networkHandler.sendPacket(new PlayerRespawnS2CPacket(serverPlayerEntity.createCommonPlayerSpawnInfo(serverWorld), (byte) 0));
        serverPlayerEntity.networkHandler.requestTeleport(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), serverPlayerEntity.getYaw(), serverPlayerEntity.getPitch());
        serverPlayerEntity.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(serverWorld.getSpawnPos(), serverWorld.getSpawnAngle()));
        serverPlayerEntity.networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        serverPlayerEntity.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(serverPlayerEntity.experienceProgress, serverPlayerEntity.totalExperience, serverPlayerEntity.experienceLevel));
        this.sendWorldInfo(serverPlayerEntity, serverWorld);
        this.sendCommandTree(serverPlayerEntity);
        serverWorld.onPlayerRespawned(serverPlayerEntity);
        this.players.add(serverPlayerEntity);
        this.playerMap.put(serverPlayerEntity.getUuid(), serverPlayerEntity);
        serverPlayerEntity.onSpawn();
        serverPlayerEntity.setHealth(serverPlayerEntity.getHealth());
        return serverPlayerEntity;
    }
}
