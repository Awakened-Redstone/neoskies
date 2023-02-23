package skylands.mixin.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.SkylandsMain;
import skylands.util.WorldProtection;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements StructureWorldAccess {

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

     @Inject(method = "spawnEntity", at = @At("HEAD"), cancellable = true)
    private void spawnEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
         if (!SkylandsMain.MAIN_CONFIG.getConfig().disableEntitiesOutsideIslands) return;
        if (entity instanceof ItemEntity || entity instanceof ProjectileEntity) return;
        World world = entity.getWorld();
        BlockPos blockPos = entity.getBlockPos();
        if (!world.isClient) {
            if (!WorldProtection.isWithinIsland(world, blockPos)) {
                cir.setReturnValue(false);
            }
        }
    }

}
