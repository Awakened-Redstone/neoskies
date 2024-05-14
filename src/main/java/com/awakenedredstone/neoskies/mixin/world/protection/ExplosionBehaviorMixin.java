package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import com.awakenedredstone.neoskies.util.WorldProtection;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ExplosionBehavior.class, EntityExplosionBehavior.class})
public class ExplosionBehaviorMixin {
    @Inject(method = "canDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void protectIslands(Explosion explosion, BlockView view, BlockPos pos, BlockState state, float power, CallbackInfoReturnable<Boolean> cir) {
        if (!(view instanceof World world)) {
            return;
        }

        PlayerEntity player = null;
        LivingEntity entity = explosion.getCausingEntity();

        if (entity instanceof PlayerEntity) {
            player = (PlayerEntity) entity;
        } else if (entity instanceof Ownable ownable && ownable.getOwner() instanceof PlayerEntity owner) {
            player = owner;
        }

        if (player == null) {
            return;
        }

        boolean isBlockTrigger = explosion.getDestructionType() == Explosion.DestructionType.TRIGGER_BLOCK;
        IslandSettings settings = isBlockTrigger ? NeoSkiesIslandSettings.USE_REDSTONE : NeoSkiesIslandSettings.BREAK_BLOCKS;

        if (!WorldProtection.canModify(world, pos, player, settings)) {
            cir.setReturnValue(false);
        }
    }
}
