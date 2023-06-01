package skylands.duck;

import net.minecraft.world.chunk.ChunkSection;

import java.util.Set;

public interface ExtendedChunk {
    Set<ChunkSection> getNonEmptySections();
}
