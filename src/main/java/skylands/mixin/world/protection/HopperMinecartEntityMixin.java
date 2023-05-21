package skylands.mixin.world.protection;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skylands.util.WorldProtection;

@Mixin(HopperMinecartEntity.class)
public abstract class HopperMinecartEntityMixin extends StorageMinecartEntity {

    protected HopperMinecartEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "isEnabled", at = @At("TAIL"), cancellable = true)
    private void lockMinecartHopper(CallbackInfoReturnable<Boolean> cir) {
        if (!WorldProtection.isWithinIsland(world, getBlockPos())) {
            cir.setReturnValue(false);
        }
    }
}
