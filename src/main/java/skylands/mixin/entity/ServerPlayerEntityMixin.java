package skylands.mixin.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skylands.api.SkylandsAPI;
import skylands.event.PlayerEvents;
import skylands.util.Worlds;

import java.util.Optional;

//TODO: Update chunks around the player
//TODO: Fix it not redirecting when the player joins
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "createEndSpawnPlatform", at = @At("HEAD"), cancellable = true)
    public void blockEndPlatform(CallbackInfo ci) {
        if (SkylandsAPI.isIsland(world)) {
            ci.cancel();
        }
    }

    @Redirect(method = "getPortalRect", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z", ordinal = 1))
    public boolean skipErrorMessage(Optional instance) {
        if (SkylandsAPI.isIsland(world)) {
            return true;
        }
        return instance.isPresent();
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

    @Inject(method = "tick", at = @At(value = "HEAD"))
    public void tick(CallbackInfo ci) {
        PlayerEvents.TICK.invoker().onPlayerTick((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        PlayerEvents.POST_DEATH.invoker().onPlayerPostDeath((ServerPlayerEntity) (Object) this);
    }

}
