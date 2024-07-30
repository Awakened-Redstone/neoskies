package com.awakenedredstone.neoskies.mixin.entity;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.util.Worlds;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow private World world;
    @Shadow public abstract Vec3d getVelocity();
    @Shadow public abstract float getYaw();
    @Shadow public abstract float getPitch();

    @ModifyVariable(method = "tickPortal", at = @At("STORE"), ordinal = 0)
    public RegistryKey<World> tickPortal_modifyRegistryKey(RegistryKey<World> instance) {
        if (NeoSkiesAPI.isIsland(world) && !NeoSkiesAPI.isNether(world.getRegistryKey())) {
            Optional<Island> island = NeoSkiesAPI.getOptionalIsland(world);
            if (island.isPresent()) {
                return island.get().getNether().getRegistryKey();
            }
        }
        if (NeoSkiesAPI.isIsland(world) && NeoSkiesAPI.isNether(world.getRegistryKey())) {
            Optional<Island> island = NeoSkiesAPI.getOptionalIsland(world);
            if (island.isPresent()) {
                return island.get().getOverworld().getRegistryKey();
            }
        }
        return instance;
    }

    @ModifyExpressionValue(method = "getTeleportTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"))
    public RegistryKey<World> getTeleportTarget_redirectRegistryKey0(RegistryKey<World> world) {
        return Worlds.redirect(world);
    }

    @ModifyExpressionValue(method = "getTeleportTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"))
    public RegistryKey<World> getTeleportTarget_redirectRegistryKey1(RegistryKey<World> world) {
        return Worlds.redirect(world);
    }

    @Inject(method = "getTeleportTarget", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    public void fixEndTeleportTarget(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir) {
        if (NeoSkiesAPI.isIsland(world)) {
            Island island = NeoSkiesAPI.getOptionalIsland(world).get();
            Vec3d spawnPos = island.spawnPos;
            cir.setReturnValue(new TeleportTarget(spawnPos, getVelocity(), getYaw(), getPitch()));
        }
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "getTeleportTarget", at = @At(value = "RETURN", ordinal = 2), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void teleportProtection(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir, boolean bl, WorldBorder worldBorder, double d, BlockPos blockPos2) {
        if (NeoSkiesAPI.isIsland(world)) {
            Island island = NeoSkiesAPI.getOptionalIsland(world).get();
            if (!island.isWithinBorder(blockPos2)) {
                Vec3d spawnPos = island.spawnPos;
                cir.setReturnValue(new TeleportTarget(spawnPos, getVelocity(), getYaw(), getPitch()));
            }
        }
    }

    @ModifyExpressionValue(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/registry/RegistryKey;", ordinal = 0))
    public RegistryKey<World> moveToWorld_redirectRegistryKey(RegistryKey<World> world) {
        return Worlds.redirect(world);
    }
}
