package com.awakenedredstone.neoskies.logic;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.data.BlockGeneratorLoader;
import com.awakenedredstone.neoskies.event.GenericEntityDamageEvent;
import com.awakenedredstone.neoskies.event.PlayerConnectEvent;
import com.awakenedredstone.neoskies.event.PlayerEvents;
import com.awakenedredstone.neoskies.event.ServerEventListener;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import com.awakenedredstone.neoskies.logic.tags.NeoSkiesItemTags;
import com.awakenedredstone.neoskies.util.ServerUtils;
import com.awakenedredstone.neoskies.util.WorldProtection;
import com.awakenedredstone.neoskies.util.Worlds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.BlockTrampleEvent;
import xyz.nucleoid.stimuli.event.block.FlowerPotModifyEvent;
import xyz.nucleoid.stimuli.event.entity.EntityShearEvent;
import xyz.nucleoid.stimuli.event.entity.EntityUseEvent;

import java.util.Map;

public class EventListeners {
    public static void listenForEvents() {
        protectionEvents();

        ServerLifecycleEvents.SERVER_STARTING.register(ServerEventListener::onStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(ServerEventListener::onStop);
        ServerTickEvents.END_SERVER_TICK.register(ServerEventListener::onTick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PlayerConnectEvent.onJoin(server, handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PlayerConnectEvent.onLeave(server, handler.player));

        PlayerEvents.TICK.register(player -> {
            if (player.getY() < player.getWorld().getBottomY() - IslandLogic.getConfig().safeVoidBlocksBelow) {
                if ((IslandLogic.getConfig().safeVoid && NeoSkiesAPI.getOptionalIsland(player.getWorld()).isPresent()) || NeoSkiesAPI.isHub(player.getWorld())) {
                    player.server.execute(() -> Worlds.returnToIslandSpawn(player, IslandLogic.getConfig().safeVoidFallDamage || !NeoSkiesAPI.isHub(player.getWorld())));
                }
            }
        });

        ResourceManagerHelper resourceManagerHelper = ResourceManagerHelper.get(ResourceType.SERVER_DATA);
        resourceManagerHelper.registerReloadListener(BlockGeneratorLoader.INSTANCE);
    }

    private static void protectionEvents() {
        Stimuli.global().listen(BlockPlaceEvent.BEFORE, (player, world, pos, state, context) -> {
            BlockPos blockPos = context.getBlockPos();
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

        Stimuli.global().listen(GenericEntityDamageEvent.EVENT, EventListeners::onEntityDamage);

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
