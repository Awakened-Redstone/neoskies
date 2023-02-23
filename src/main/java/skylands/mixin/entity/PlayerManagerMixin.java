package skylands.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import skylands.logic.Island;
import skylands.logic.Skylands;
import skylands.util.Worlds;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow @Final private List<ServerPlayerEntity> players;
    @Shadow @Final private Map<UUID, ServerPlayerEntity> playerMap;
    @Shadow @Final private MinecraftServer server;

    @Shadow public abstract void sendWorldInfo(ServerPlayerEntity player, ServerWorld world);
    @Shadow public abstract void sendCommandTree(ServerPlayerEntity player);

    /**
     * @author Skylands (custom)
     * @reason Island respawn
     */
    @Overwrite
    public ServerPlayerEntity respawnPlayer(ServerPlayerEntity player, boolean alive) {
        this.players.remove(player);
        player.getWorld().removePlayer(player, Entity.RemovalReason.DISCARDED);
        Vec3d vec3d = ((Supplier<Vec3d>)(() -> {
            if (Worlds.isIsland(player.world)) {
                Optional<Island> islandOptional = Worlds.getIsland(player.world);
                if (islandOptional.isPresent()) {
                    Island island = islandOptional.get();
                    if (island.isMember(player)) {
                        return island.spawnPos;
                    } else {
                        return island.visitsPos;
                    }
                }
            }
            return Skylands.getInstance().hub.pos;
        })).get();
        BlockPos blockPos = new BlockPos(vec3d);
        float f = player.getSpawnAngle();
        boolean bl = player.isSpawnForced();
        ServerWorld serverWorld = this.server.getWorld(Worlds.isIsland(player.world) ? player.world.getRegistryKey() : World.OVERWORLD);
        ServerPlayerEntity serverPlayerEntity = new ServerPlayerEntity(this.server, serverWorld, player.getGameProfile());
        serverPlayerEntity.networkHandler = player.networkHandler;
        serverPlayerEntity.copyFrom(player, alive);
        serverPlayerEntity.setId(player.getId());
        serverPlayerEntity.setMainArm(player.getMainArm());
        for (String string : player.getScoreboardTags()) {
            serverPlayerEntity.addScoreboardTag(string);
        }
        serverPlayerEntity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, 0, 0);
        serverPlayerEntity.setSpawnPoint(serverWorld.getRegistryKey(), blockPos, f, bl, false);

        WorldProperties worldProperties = serverPlayerEntity.world.getLevelProperties();
        serverPlayerEntity.networkHandler.sendPacket(new PlayerRespawnS2CPacket(serverPlayerEntity.world.getDimensionKey(), Worlds.redirect(serverPlayerEntity.world.getRegistryKey()), BiomeAccess.hashSeed(serverPlayerEntity.getWorld().getSeed()), serverPlayerEntity.interactionManager.getGameMode(), serverPlayerEntity.interactionManager.getPreviousGameMode(), serverPlayerEntity.getWorld().isDebugWorld(), serverPlayerEntity.getWorld().isFlat(), (byte) (alive ? 1 : 0), serverPlayerEntity.getLastDeathPos()));
        serverPlayerEntity.networkHandler.requestTeleport(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), serverPlayerEntity.getYaw(), serverPlayerEntity.getPitch());
        serverPlayerEntity.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(blockPos, serverWorld.getSpawnAngle()));
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
