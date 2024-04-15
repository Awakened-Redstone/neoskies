package com.awakenedredstone.neoskies.data;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import com.awakenedredstone.neoskies.logic.Skylands;

public record WorldComponent(World world) implements ComponentV3 {

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            Skylands.getInstance().readFromNbt(nbt);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            Skylands.getInstance().writeToNbt(nbt);
        }
    }
}
