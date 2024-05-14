package com.awakenedredstone.neoskies.data;

import com.awakenedredstone.neoskies.logic.Skylands;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentV3;

public record WorldComponent(World world) implements ComponentV3 {

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            Skylands.getInstance().readFromNbt(nbt);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            Skylands.getInstance().writeToNbt(nbt);
        }
    }
}
