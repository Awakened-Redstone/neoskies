package com.awakenedredstone.neoskies.mixin.entity;

import com.awakenedredstone.neoskies.api.SkylandsAPI;
import com.awakenedredstone.neoskies.duck.ExtendedServerPlayerEntity;
import com.awakenedredstone.neoskies.event.PlayerEvents;
import com.awakenedredstone.neoskies.util.Worlds;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ExtendedServerPlayerEntity {

    @Shadow
    private boolean filterText;

    @Shadow
    private @Nullable PublicPlayerSession session;

    @Shadow
    @Final
    public ServerPlayerInteractionManager interactionManager;

    @Shadow
    private int syncedExperience;

    @Shadow
    private float syncedHealth;

    @Shadow
    private int syncedFoodLevel;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "createEndSpawnPlatform", at = @At("HEAD"), cancellable = true)
    public void blockEndPlatform(CallbackInfo ci) {
        if (SkylandsAPI.isIsland(getWorld())) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "getPortalRect", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isEmpty()Z"))
    public boolean skipErrorMessage(boolean original) {
        if (SkylandsAPI.isIsland(getWorld())) {
            return false;
        }

        return original;
    }

    @Redirect(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/registry/RegistryKey;", ordinal = 1))
    public RegistryKey<World> moveToWorld_blockRedirectRegistryKey(ServerWorld instance) {
        return instance.getRegistryKey();
    }

    @Redirect(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"))
    public RegistryKey<World> moveToWorld_redirectRegistryKey(ServerWorld instance) {
        return Worlds.redirect(instance.getRegistryKey());
    }

    @Redirect(method = "getTeleportTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"))
    public RegistryKey<World> getTeleportTarget_redirectRegistryKey(ServerWorld instance) {
        return Worlds.redirect(instance.getRegistryKey());
    }

    @Redirect(method = "worldChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"))
    public RegistryKey<World> worldChanged_redirectRegistryKey(ServerWorld instance) {
        return Worlds.redirect(instance.getRegistryKey());
    }
    @Redirect(method = "createCommonPlayerSpawnInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"))
    public RegistryKey<World> createCommonPlayerSpawnInfo_redirectRegistryKey(ServerWorld instance) {
        return Worlds.redirect(instance.getRegistryKey());
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    public void tick(CallbackInfo ci) {
        PlayerEvents.TICK.invoker().onPlayerTick((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        PlayerEvents.POST_DEATH.invoker().onPlayerPostDeath((ServerPlayerEntity) (Object) this);
    }

    @Override
    public void neoskies$simpleCopyFrom(ServerPlayerEntity oldPlayer) {
        this.filterText = oldPlayer.shouldFilterText();
        this.session = oldPlayer.getSession();
        this.interactionManager.setGameMode(oldPlayer.interactionManager.getGameMode(), oldPlayer.interactionManager.getPreviousGameMode());
        this.sendAbilitiesUpdate();
        this.syncedExperience = -1;
        this.syncedHealth = -1.0f;
        this.syncedFoodLevel = -1;
    }
}
