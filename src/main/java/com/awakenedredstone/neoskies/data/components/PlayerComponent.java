package com.awakenedredstone.neoskies.data.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.ComponentV3;

import java.util.ArrayList;

public class PlayerComponent implements ComponentV3 {
    public final PlayerEntity player;
    private ArrayList<String> islands = new ArrayList<>();

    public PlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    public ArrayList<String> getIslands() {
        return islands;
    }

    public void setIslands(ArrayList<String> islands) {
        this.islands = islands;
    }

    public void addIsland(String owner) {
        if (!this.islands.contains(owner)) {
            islands.add(owner);
        }
    }

    public void removeIsland(String owner) {
        islands.removeIf(s -> s.equals(owner));
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound islandsNbt = tag.getCompound("islands");
        int size = islandsNbt.getInt("size");
        for (int i = 0; i < size; i++) {
            String owner = islandsNbt.getString(String.valueOf(i));
            this.islands.add(owner);
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound islandsNbt = new NbtCompound();
        islandsNbt.putInt("size", this.islands.size());
        for (int i = 0; i < this.islands.size(); i++) {
            String owner = this.islands.get(i);
            islandsNbt.putString(Integer.toString(i), owner);
        }
        tag.put("islands", islandsNbt);
    }
}
