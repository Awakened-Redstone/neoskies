package com.awakenedredstone.neoskies.mixin.world.protection;

import com.awakenedredstone.neoskies.event.GenericEntityDamageEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.stimuli.Stimuli;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract World getWorld();

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (this.getWorld().isClient) {
            return;
        }

        var entity = (Entity) (Object) this;

        try (var invokers = Stimuli.select().forEntity(entity)) {
            var result = invokers.get(GenericEntityDamageEvent.EVENT).onDamage(entity, source, -1);
            if (result == ActionResult.FAIL) {
                cir.setReturnValue(true);
            }
        }
    }
}
