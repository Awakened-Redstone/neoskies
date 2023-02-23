package skylands.mixin.world.protection;

import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skylands.util.WorldProtection;

import static skylands.util.ServerUtils.protectionWarning;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin extends ThrownItemEntity {

    public PotionEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "extinguishFire", at = @At("HEAD"), cancellable = true)
    void interact(BlockPos pos, CallbackInfo ci) {
        if (!world.isClient) {
            if ((getOwner() instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player))) {
                BlockState blockState = this.world.getBlockState(pos);
                if (blockState.isIn(BlockTags.FIRE) || AbstractCandleBlock.isLitCandle(blockState) || CampfireBlock.isLitCampfire(blockState)) {
                    protectionWarning(player, "extinguish");
                }
                ci.cancel();
            } else if (!WorldProtection.isWithinIsland(world, pos)) {
                ci.cancel();
            }
        }
    }
}
