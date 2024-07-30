package com.awakenedredstone.neoskies.mixin.block.entity;

import com.awakenedredstone.neoskies.util.Worlds;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndGatewayBlockEntity.class)
public class EndGatewayBlockEntityMixin {
    @ModifyExpressionValue(method = "tryTeleportingEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"))
    private static RegistryKey<World> tryTeleportingEntity_redirectRegistryKey(RegistryKey<World> world) {
        return Worlds.redirect(world);
    }
}
