package com.awakenedredstone.neoskies.mixin.world;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.mixin.block.accessor.WorldBorderAccessor;
import com.awakenedredstone.neoskies.util.Worlds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnreachableCode")
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Unique private double lastSize = -1;
    @Unique private Vec3d lastPos = Vec3d.ZERO;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    /* TODO: ======= OPTIMISE THIS ======= */
    // TODO: Add other visual ways to show the limit
    @Inject(method = "travel", at = @At("HEAD"))
    private void increaseTravelMotionStats(Vec3d movementInput, CallbackInfo ci) {
        if (((PlayerEntity) (Object) this) instanceof ServerPlayerEntity serverPlayer && NeoSkiesAPI.isIsland(getWorld()) && !lastPos.equals(getPos()) && getWorld() != null) {
            WorldBorder defaultWorldBorder = getWorld().getWorldBorder();
            Island island = NeoSkiesAPI.getOptionalIsland(getWorld()).get();
            if (island.radius <= 0) {
                if (lastSize != -1) {
                    WorldBorder border = new WorldBorder();
                    border.setCenter(0, 0);
                    WorldBorderAccessor borderAccessor = (WorldBorderAccessor) border;
                    borderAccessor.setArea(border.new StaticArea(defaultWorldBorder.getSize()));
                    serverPlayer.networkHandler.sendPacket(new WorldBorderInterpolateSizeS2CPacket(border));
                    lastSize = -1;
                }
                return;
            }

            lastPos = getPos();
            double x = Math.abs(getX());
            double z = Math.abs(getZ());
            int range = 64;

            if (x > island.radius + range + 8 || z > island.radius + range + 8) {
                Worlds.returnToIslandSpawn(serverPlayer, false);
                return;
            }
            double oldSize = lastSize == -1 ? defaultWorldBorder.getSize() : lastSize;
            WorldBorder border = new WorldBorder();
            border.setCenter(0, 0);
            WorldBorderAccessor borderAccessor = (WorldBorderAccessor) border;
            int finalRadius = island.radius + range;
            lastSize = Math.max(finalRadius, Math.abs(calculateBorderSize(island.radius, range))) * 2;
            if (x > finalRadius || z > finalRadius) lastSize = finalRadius * 2;
            borderAccessor.setArea(border.new MovingArea(oldSize, lastSize, /*serverPlayer.pingMilliseconds +*/ 100));
            serverPlayer.networkHandler.sendPacket(new WorldBorderInterpolateSizeS2CPacket(border));
        }
    }

    @Unique
    private double calculateBorderSize(int islandRadius, int range) {
        int scale = islandRadius + range - 2;
        double in = islandRadius + range + 128;
        double x = Math.abs(getX());
        double z = Math.abs(getZ());
        if (x > islandRadius && z > islandRadius) {
            double xLerp = MathHelper.lerp(scaleDown(0, scale, x), in, scale);
            double zLerp = MathHelper.lerp(scaleDown(0, scale, z), in, scale);
            return Math.min(xLerp, zLerp);
        } else if (x > islandRadius) {
            return MathHelper.lerp(scaleDown(0, scale, x), in, scale);
        } else if (z > islandRadius) {
            return MathHelper.lerp(scaleDown(0, scale, z), in, scale);
        } else {
            return in;
        }
    }

    @Unique
    @SuppressWarnings("SameParameterValue")
    private static double scaleDown(double start, double end, double delta) {
        double t = (delta - start) / (end - start);
        return -1 * t * (t - 2);
    }
}
