package com.awakenedredstone.neoskies.logic.util;

import net.minecraft.world.chunk.Chunk;

import java.util.LinkedList;

public class ChunkScanQueue {
    private final LinkedList<Chunk> queue = new LinkedList<>();
    private int pos = 0;

    /**
     * Adds a chunk to the queue.
     *
     * @param chunk The {@link Chunk} to add.
     * @return {@code true} if the chunk was added, {@code false} otherwise.
     **/
    public boolean add(Chunk chunk) {
        if (queue.contains(chunk)) return false;
        return queue.add(chunk);
    }

    /**
     * Checks if the queue is empty.
     *
     * @return {@code true} if the queue is empty, {@code false} otherwise.
     **/
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean finished() {
        return pos >= queue.size();
    }

    /**
     * Gets the size of the queue.
     *
     * @return The size of the queue.
     **/
    public int size() {
        return queue.size();
    }

    /**
     * Removes a {@link Chunk} from the queue.
     *
     * @param chunk The chunk to remove.
     * @return {@code true} if the chunk was removed, {@code false} otherwise.
     **/
    public boolean remove(Chunk chunk) {
        return queue.remove(chunk);
    }

    /**
     * Clears the queue.
     *
     * @return {@code true} if something was removed, {@code false} otherwise.
     **/
    public boolean clear() {
        final boolean result = !queue.isEmpty();
        queue.clear();
        return result;
    }

    /**
     * Retrieves and removes the next chunk in the queue.
     *
     * @return The next {@link Chunk} in the queue, or {@code null} if the queue is empty.
     **/
    public Chunk poll() {
        if (pos >= queue.size()) return null;
        return queue.get(pos++);
    }

    /**
     * Gets the next chunk in the queue without removing it.
     *
     * @return The next {@link Chunk} in the queue, or {@code null} if the queue is empty.
     **/
    public Chunk peek() {
        if (pos >= queue.size()) return null;
        return queue.get(pos);
    }

    /**
     * Skips the next {@link Chunk} in the queue.
     **/
    public void skip() {
        pos++;
    }

    /**
     * Gets the current position in the queue.
     *
     * @return The current position in the queue.
     **/
    public int getPos() {
        return pos;
    }

    /**
     * Resets the queue to the beginning.
     **/
    public void reset() {
        pos = 0;
    }
}
