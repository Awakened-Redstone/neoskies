package com.awakenedredstone.neoskies.mixin.world.protection.item;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import com.awakenedredstone.neoskies.util.ServerUtils;
import com.awakenedredstone.neoskies.util.WorldProtection;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BucketItem.class)
public class BucketItemMixin {
    @Shadow @Final private Fluid fluid;

    @Inject(method = "use", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/hit/BlockHitResult;getBlockPos()Lnet/minecraft/util/math/BlockPos;", shift = At.Shift.AFTER), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    void use(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir, ItemStack stack, BlockHitResult blockHitResult) {
        if (!world.isClient()) {
            boolean isEmpty = this.fluid == Fluids.EMPTY;
            if (isEmpty) {
                BlockState state = world.getBlockState(blockHitResult.getBlockPos());
                if (!(state.getBlock() instanceof FluidDrainable)) return;
            }
            IslandSettings settings = isEmpty ? NeoSkiesIslandSettings.BREAK_BLOCKS : NeoSkiesIslandSettings.PLACE_BLOCKS;
            BlockPos blockPos = BlockPos.ofFloored(blockHitResult.getPos());
            if (!WorldProtection.canModify(world, blockPos, player, settings)) {
                ServerUtils.protectionWarning(player, settings);
                int slot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                cir.setReturnValue(TypedActionResult.fail(player.getStackInHand(hand)));
            }
        }
    }
}
