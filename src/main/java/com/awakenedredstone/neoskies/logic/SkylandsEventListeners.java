package com.awakenedredstone.neoskies.logic;

import com.awakenedredstone.neoskies.api.SkylandsAPI;
import com.awakenedredstone.neoskies.event.PlayerConnectEvent;
import com.awakenedredstone.neoskies.event.PlayerEvents;
import com.awakenedredstone.neoskies.event.ServerEventListener;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import com.awakenedredstone.neoskies.util.ServerUtils;
import com.awakenedredstone.neoskies.util.WorldProtection;
import com.awakenedredstone.neoskies.util.Worlds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.BlockTrampleEvent;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
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

        Stimuli.global().listen(BlockUseEvent.EVENT, (player, hand, hitResult) -> {
            World world = player.getWorld();
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
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        Stimuli.global().listen(BlockPlaceEvent.BEFORE, (player, world, pos, state, context) -> {
            BlockPos blockPos = context.getBlockPos().offset(context.getSide());
            if (!WorldProtection.canModify(world, blockPos, player, NeoSkiesIslandSettings.PLACE_BLOCKS)) {
                ServerUtils.protectionWarning(player, "neoskies.place_blocks");
                int slot = context.getHand() == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                var stack = context.getStack();
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, 0, slot, stack));
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        Stimuli.global().listen(BlockBreakEvent.EVENT, (player, world, pos) -> {
            if (!WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.BREAK_BLOCKS)) {
                ServerUtils.protectionWarning(player, "neoskies.break_blocks");
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        Stimuli.global().listen(BlockTrampleEvent.EVENT, (entity, world, pos, from, to) -> {
            if (entity instanceof PlayerEntity player && !WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.BREAK_BLOCKS)) {
                ServerUtils.protectionWarning(player, "neoskies.break_blocks");
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        Stimuli.global().listen(PlayerAttackEntityEvent.EVENT, (attacker, hand, attacked, hitResult) -> {
            boolean monster = attacked instanceof Monster;
            IslandSettings rule = monster ? NeoSkiesIslandSettings.HURT_HOSTILE : NeoSkiesIslandSettings.HURT_PASSIVE;
            if (!WorldProtection.canModify(attacker.getWorld(), attacked.getBlockPos(), attacker, rule)) {
                ServerUtils.protectionWarning(attacker, monster ? "neoskies.hurt_hostile" : "neoskies.hurt_passive");
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        Stimuli.global().listen(EntityShearEvent.EVENT, (entity, player, hand, pos) -> {
            if (!WorldProtection.canModify(player.getWorld(), pos, player, NeoSkiesIslandSettings.SHEAR_ENTITY)) {
                ServerUtils.protectionWarning(player, "entity_shear");
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        Stimuli.global().listen(EntityUseEvent.EVENT, (player, entity, hand, hitResult) -> {
            for (Map.Entry<TagKey<EntityType<?>>, IslandSettings> entry : NeoSkiesIslandSettings.getRuleEntityTags().entrySet()) {
                if (entity.getType().isIn(entry.getKey())) {
                    if (!WorldProtection.canModify(player.getWorld(), entity.getBlockPos(), player, entry.getValue())) {
                        ServerUtils.protectionWarning(player, entry.getValue().getTranslationKey());
                        return ActionResult.FAIL;
                    }
                }
            }

            ItemStack stack = player.getStackInHand(hand);
            if (stack.isOf(Items.LEAD) && !WorldProtection.canModify(player.getWorld(), entity.getBlockPos(), player, NeoSkiesIslandSettings.LEASH_ENTITY)) {
                ServerUtils.protectionWarning(player, NeoSkiesIslandSettings.LEASH_ENTITY.getTranslationKey());
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
    }
}
