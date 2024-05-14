package com.awakenedredstone.neoskies.mixin.entity;

import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataTracker.class)
public interface DataTrackerAccessor {
    @Accessor DataTracker.Entry<?>[] getEntries();
}
