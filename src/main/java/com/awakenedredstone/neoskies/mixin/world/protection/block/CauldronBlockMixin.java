package com.awakenedredstone.neoskies.mixin.world.protection.block;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.util.ServerUtils;
import com.awakenedredstone.neoskies.util.WorldProtection;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCauldronBlock.class)
public class CauldronBlockMixin {
    @Shadow @Final protected CauldronBehavior.CauldronBehaviorMap behaviorMap;

    @Inject(method = "onUseWithItem", at = @At("HEAD"), cancellable = true)
    void onUse(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ItemActionResult> cir) {
        if (!world.isClient()) {
            if (behaviorMap.map().containsKey(stack.getItem()) && !WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.USE_CONTAINERS)) {
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.USE_CONTAINERS);
                cir.setReturnValue(ItemActionResult.FAIL);
            }
        }
    }
}
