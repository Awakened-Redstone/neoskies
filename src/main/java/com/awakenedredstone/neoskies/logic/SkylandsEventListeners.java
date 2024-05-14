package com.awakenedredstone.neoskies.logic;

import com.awakenedredstone.neoskies.api.SkylandsAPI;
import com.awakenedredstone.neoskies.event.*;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import com.awakenedredstone.neoskies.logic.tags.NeoSkiesItemTags;
import com.awakenedredstone.neoskies.util.ServerUtils;
import com.awakenedredstone.neoskies.util.WorldProtection;
import com.awakenedredstone.neoskies.util.Worlds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.block.*;
import xyz.nucleoid.stimuli.event.entity.EntityDamageEvent;
import xyz.nucleoid.stimuli.event.entity.EntityShearEvent;
import xyz.nucleoid.stimuli.event.entity.EntityUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerAttackEntityEvent;

import java.util.Map;

public class SkylandsEventListeners {
    public static void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerEventListener::onStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(ServerEventListener::onStop);
        ServerTickEvents.END_SERVER_TICK.register(ServerEventListener::onTick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PlayerConnectEvent.onJoin(server, handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PlayerConnectEvent.onLeave(server, handler.player));

        PlayerEvents.TICK.register(player -> {
            if (player.getY() < player.getWorld().getBottomY() - Skylands.getConfig().safeVoidBlocksBelow) {
                if ((Skylands.getConfig().safeVoid && SkylandsAPI.getIsland(player.getWorld()).isPresent()) || SkylandsAPI.isHub(player.getWorld())) {
                    player.server.execute(() -> Worlds.returnToIslandSpawn(player, Skylands.getConfig().safeVoidFallDamage || !SkylandsAPI.isHub(player.getWorld())));
                }
            }
        });

        /*Stimuli.global().listen(BlockUseEvent.EVENT, (player, hand, hitResult) -> {
            World world = player.getWorld();
            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isEmpty() && player.isSneaking()) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            IslandSettings settings = null;

            for (Map.Entry<TagKey<Block>, IslandSettings> entry : NeoSkiesIslandSettings.getRuleBlockTags().entrySet()) {
                if (state.isIn(entry.getKey())) {
                    settings = entry.getValue();
                    break;
                }
            }

            if (settings == null) {
                return ActionResult.PASS;
            }

            if (!WorldProtection.canModify(world, pos, player, settings)) {
                ServerUtils.protectionWarning(player, settings.getTranslationKey());
                int slot = hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });*/

        Stimuli.global().listen(BlockPlaceEvent.BEFORE, (player, world, pos, state, context) -> {
            BlockPos blockPos = context.getBlockPos().offset(context.getSide());
            if (!WorldProtection.canModify(world, blockPos, player, NeoSkiesIslandSettings.PLACE_BLOCKS)) {
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.PLACE_BLOCKS);
                int slot = context.getHand() == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                var stack = context.getStack();
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        Stimuli.global().listen(BlockBreakEvent.EVENT, (player, world, pos) -> {
            if (!WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.BREAK_BLOCKS)) {
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.BREAK_BLOCKS);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        Stimuli.global().listen(BlockTrampleEvent.EVENT, (entity, world, pos, from, to) -> {
            if (entity instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.BREAK_BLOCKS)) {
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.BREAK_BLOCKS);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        Stimuli.global().listen(GenericEntityDamageEvent.EVENT, SkylandsEventListeners::onEntityDamage);

        Stimuli.global().listen(EntityShearEvent.EVENT, (entity, player, hand, pos) -> {
            if (!WorldProtection.canModify(player.getWorld(), pos, player, NeoSkiesIslandSettings.SHEAR_ENTITY)) {
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.SHEAR_ENTITY);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        Stimuli.global().listen(EntityUseEvent.EVENT, (player, entity, hand, hitResult) -> {
            for (Map.Entry<TagKey<EntityType<?>>, IslandSettings> entry : NeoSkiesIslandSettings.getRuleEntityTags().entrySet()) {
                if (entity.getType().isIn(entry.getKey())) {
                    if (!WorldProtection.canModify(player.getWorld(), entity.getBlockPos(), player, entry.getValue())) {
                        ServerUtils.protectionWarning(player, entry.getValue());
                        return ActionResult.FAIL;
                    }
                }
            }

            ItemStack stack = player.getStackInHand(hand);
            if (stack.isIn(NeoSkiesItemTags.LEAD) && !WorldProtection.canModify(player.getWorld(), entity.getBlockPos(), player, NeoSkiesIslandSettings.LEASH_ENTITY)) {
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.LEASH_ENTITY);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        Stimuli.global().listen(FlowerPotModifyEvent.EVENT, (player, hand, hitResult) -> {
            if (!WorldProtection.canModify(player.getWorld(), hitResult.getBlockPos(), player, NeoSkiesIslandSettings.PLACE_BLOCKS)) {
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.PLACE_BLOCKS);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
    }

    private static ActionResult onEntityDamage(Entity entity, DamageSource source, float amount) {
        PlayerEntity player = null;
        Entity attacker = source.getAttacker();

        if (attacker instanceof PlayerEntity) {
            player = (PlayerEntity) attacker;
        } else if (attacker instanceof Ownable ownable && ownable.getOwner() instanceof PlayerEntity owner) {
            player = owner;
        }

        if (player == null) {
            return ActionResult.PASS;
        }

        boolean monster = entity instanceof Monster;
        IslandSettings rule = monster ? NeoSkiesIslandSettings.HURT_HOSTILE : NeoSkiesIslandSettings.HURT_PASSIVE;
        if (!WorldProtection.canModify(attacker.getWorld(), entity.getBlockPos(), player, rule)) {
            ServerUtils.protectionWarning(player, rule);
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }
}
