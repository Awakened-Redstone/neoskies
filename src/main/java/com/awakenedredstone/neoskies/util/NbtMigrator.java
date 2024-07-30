package com.awakenedredstone.neoskies.util;

import net.minecraft.nbt.NbtCompound;

public class NbtMigrator {
    public static void update(NbtCompound nbt) {
        int format = nbt.getInt("format");
    }
}
