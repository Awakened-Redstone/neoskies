package skylands.mixin.world;

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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skylands.api.SkylandsAPI;
import skylands.logic.Island;
import skylands.mixin.block.accessor.WorldBorderAccessor;
import skylands.util.Worlds;

/* TODO: ======= OPTIMIZE THIS ======= */
/* TODO: ======= OPTIMIZE THIS ======= */
/* TODO: ======= OPTIMIZE THIS ======= */
@SuppressWarnings("ConstantValue")
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    private double lastSize = -1;
    private Vec3d lastPos = Vec3d.ZERO;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    /* TODO: ======= OPTIMIZE THIS ======= */
    /* TODO: ======= OPTIMIZE THIS ======= */
    /* TODO: ======= OPTIMIZE THIS ======= */
    // TODO: Add other visual ways to show the limit
    @Inject(method = "increaseTravelMotionStats", at = @At("HEAD"))
    private void increaseTravelMotionStats(double dx, double dy, double dz, CallbackInfo ci) {
        if (((Object) this) instanceof ServerPlayerEntity serverPlayer && SkylandsAPI.getIsland(world).isPresent() && !lastPos.equals(getPos())) {
            lastPos = getPos();
            double x = Math.abs(getX());
            double z = Math.abs(getZ());
            int range = 64;
            Island island = SkylandsAPI.getIsland(world).get();
            if (x > island.radius + range + 8 || z > island.radius + range + 8) {
                Worlds.teleportToIsland(serverPlayer, false);
                return;
            }
            WorldBorder defaultWorldBorder = world.getWorldBorder();
            double oldSize = lastSize == -1 ? defaultWorldBorder.getSize() : lastSize;
            WorldBorder border = new WorldBorder();
            border.setCenter(0, 0);
            WorldBorderAccessor borderAccessor = (WorldBorderAccessor) border;
            int finalRadius = island.radius + range;
            lastSize = Math.max(finalRadius, Math.abs(calculateBorderSize(island.radius, range))) * 2;
            if (x > finalRadius || z > finalRadius) lastSize = finalRadius * 2;
            borderAccessor.setArea(border.new MovingArea(oldSize, lastSize, serverPlayer.pingMilliseconds + 100));
            serverPlayer.networkHandler.sendPacket(new WorldBorderInterpolateSizeS2CPacket(border));
        }
    }

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
        } else return in;
    }

    @SuppressWarnings("SameParameterValue")
    private static double scaleDown(double start, double end, double delta) {
        double t = (delta - start) / (end - start);
        return -1 * t * (t - 2);
    }

}
