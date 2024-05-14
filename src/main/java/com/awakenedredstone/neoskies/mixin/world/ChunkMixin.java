package com.awakenedredstone.neoskies.mixin.world;

import com.awakenedredstone.neoskies.duck.ExtendedChunk;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.Set;

@Mixin(Chunk.class)
public abstract class ChunkMixin implements ExtendedChunk {
    @Shadow
    public abstract ChunkSection[] getSectionArray();

    @Override
    public Set<ChunkSection> getNonEmptySections() {
        Set<ChunkSection> nonEmptySections = new HashSet<>();
        ChunkSection[] chunkSections = this.getSectionArray();
        for (ChunkSection chunkSection : chunkSections) {
            if (chunkSection.isEmpty()) continue;
            nonEmptySections.add(chunkSection);
        }
        return nonEmptySections;
    }
}
