package com.awakenedredstone.neoskies.mixin.accessor;

import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.StorageKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(RegionBasedStorage.class)
public interface RegionBasedStorageAccessor {
    @Accessor StorageKey getStorageKey();
    @Accessor Path getDirectory();
    @Accessor boolean getDsync();
}
