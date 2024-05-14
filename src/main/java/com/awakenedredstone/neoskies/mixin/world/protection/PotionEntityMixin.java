package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.tags.NeoSkiesBlockTags;
import com.awakenedredstone.neoskies.util.ServerUtils;
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
import com.awakenedredstone.neoskies.util.WorldProtection;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin extends ThrownItemEntity {

    public PotionEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "extinguishFire", at = @At("HEAD"), cancellable = true)
    void interact(BlockPos pos, CallbackInfo ci) {
        if (!getWorld().isClient()) {
            if ((getOwner() instanceof PlayerEntity player && !WorldProtection.canModify(getWorld(), pos, player, NeoSkiesIslandSettings.BREAK_BLOCKS))) {
                BlockState blockState = this.getWorld().getBlockState(pos);
                if (blockState.isIn(NeoSkiesBlockTags.EXTINGUISHABLE)) {
                    ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.BREAK_BLOCKS);
                }
                ci.cancel();
            }
        }
    }
}
