package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.mixin.entity.DataTrackerAccessor;
import com.awakenedredstone.neoskies.util.ServerUtils;
import com.awakenedredstone.neoskies.util.WorldProtection;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Optional;

@Mixin(Bucketable.class)
public interface BucketableMixin {
    @Inject(method = "tryBucket", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Bucketable;getBucketItem()Lnet/minecraft/item/ItemStack;", shift = At.Shift.BEFORE), cancellable = true)
    private static void preventBucket(PlayerEntity player, Hand hand, LivingEntity entity, CallbackInfoReturnable<Optional<ActionResult>> cir , @Local ItemStack stack) {
        if (player == null) return;
        World world = player.getWorld();
        if (!world.isClient()) {
            BlockPos pos = entity.getBlockPos();

            if (!WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.BUCKET_PASSIVE)) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.BUCKET_PASSIVE);
                int slot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                serverPlayer.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                serverPlayer.networkHandler.sendPacket(new EntitySpawnS2CPacket(entity));
                ArrayList<DataTracker.SerializedEntry<?>> entries = new ArrayList<>();
                for (DataTracker.Entry<?> entry : ((DataTrackerAccessor) entity.getDataTracker()).getEntries()) {
                    entries.add(entry.toSerialized());
                }
                serverPlayer.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(entity.getId(), entries));
                cir.setReturnValue(Optional.empty());
            }
        }
    }
}
