package com.awakenedredstone.neoskies.mixin.block.accessor;

import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldBorder.class)
public interface WorldBorderAccessor {
    @Accessor WorldBorder.Area getArea();
    @Accessor void setArea(WorldBorder.Area area);
}
