package com.awakenedredstone.neoskies.mixin.block;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    @ModifyVariable(method = "onEntityCollision", at = @At(value = "STORE"), ordinal = 0)
    public RegistryKey<World> resourceKey(RegistryKey<World> original, @Local(argsOnly = true) World world) {
        if (NeoSkiesAPI.isIsland(world)) {
            Optional<Island> island = NeoSkiesAPI.getOptionalIsland(world);
            if (!IslandLogic.getConfig().enableEndIsland) {
                if (island.isPresent()) {
                    return island.get().getOverworldKey();
                } else {
                    return world.getRegistryKey();
                }
            }
            if (island.isPresent()) {
                RegistryKey<World> targetWorld;
                if (NeoSkiesAPI.isEnd(world.getRegistryKey())) {
                    targetWorld = island.get().getOverworldKey();
                } else {
                    targetWorld = island.get().getEndKey();
                }
                return targetWorld;
            }
        }

        return original;
    }
}
