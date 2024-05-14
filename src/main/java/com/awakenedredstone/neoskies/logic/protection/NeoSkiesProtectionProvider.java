package com.awakenedredstone.neoskies.logic.protection;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesIslandSettings;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import com.awakenedredstone.neoskies.util.WorldProtection;
import com.mojang.authlib.GameProfile;
import eu.pb4.common.protection.api.ProtectionProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Map;

public class NeoSkiesProtectionProvider implements ProtectionProvider {
    @Override
    public boolean isProtected(World world, BlockPos pos) {
        return NeoSkiesAPI.isProtectedArea(world);
    }

    @Override
    public boolean isAreaProtected(World world, Box area) {
        return NeoSkiesAPI.isProtectedArea(world);
    }

    @Override
    public boolean canBreakBlock(World world, BlockPos pos, GameProfile profile, PlayerEntity player) {
        if (player == null) return true;
        return WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.BREAK_BLOCKS);
    }

    @Override
    public boolean canPlaceBlock(World world, BlockPos pos, GameProfile profile, PlayerEntity player) {
        if (player == null) return true;
        return WorldProtection.canModify(world, pos, player, NeoSkiesIslandSettings.PLACE_BLOCKS);
    }

    @Override
    public boolean canInteractBlock(World world, BlockPos pos, GameProfile profile, PlayerEntity player) {
        if (player == null) return true;

        BlockState state = world.getBlockState(pos);

        IslandSettings settings = null;

        for (Map.Entry<TagKey<Block>, IslandSettings> entry : NeoSkiesIslandSettings.getRuleBlockTags().entrySet()) {
            if (state.isIn(entry.getKey())) {
                settings = entry.getValue();
                break;
            }
        }

        if (settings == null) {
            return true;
        }

        return WorldProtection.canModify(world, pos, player, settings);
    }

    @Override
    public boolean canInteractEntity(World world, Entity entity, GameProfile profile, PlayerEntity player) {
        if (player == null) return true;

        for (Map.Entry<TagKey<EntityType<?>>, IslandSettings> entry : NeoSkiesIslandSettings.getRuleEntityTags().entrySet()) {
            if (entity.getType().isIn(entry.getKey())) {
                return WorldProtection.canModify(world, player, entry.getValue());
            }
        }

        return WorldProtection.canModify(world, player);
    }

    @Override
    public boolean canDamageEntity(World world, Entity entity, GameProfile profile, PlayerEntity player) {
        if (player == null) return true;
        return WorldProtection.canModify(world, player, entity instanceof Monster ? NeoSkiesIslandSettings.HURT_HOSTILE : NeoSkiesIslandSettings.HURT_PASSIVE);
    }
}
