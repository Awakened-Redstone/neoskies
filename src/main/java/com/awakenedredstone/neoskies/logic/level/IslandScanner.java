package com.awakenedredstone.neoskies.logic.level;

import com.awakenedredstone.neoskies.duck.ExtendedChunk;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.mixin.accessor.RegionBasedStorageAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.RegionFile;
import net.minecraft.world.storage.StorageIoWorker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class IslandScanner implements AutoCloseable {
    public static final Logger LOGGER = LoggerFactory.getLogger("Island Scanner");
    private final BlockingQueue<ScanSetup> scanQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("Island-Scan-Worker");
        thread.setDaemon(true); // Allow server to shut down even if scanning is in progress
        thread.setUncaughtExceptionHandler((t, e) -> {
            CrashReport crashReport = CrashReport.create(e, "Unhandled exception on island scan");
            throw new CrashException(crashReport);
        });
        return thread;
    });

    public IslandScanner() {
        executor.submit(() -> {
            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    ScanSetup islandToScan = scanQueue.take();
                    try {
                        scanIsland(islandToScan);
                    } catch (Throwable e) {
                        LOGGER.error("Failed to scan Island {}", islandToScan.island.getIslandId(), e);
                        islandToScan.errorListener().run();
                    }
                    islandToScan.island().setScanning(false);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void close() {
        executor.shutdownNow();
    }

    public void queueScan(@NotNull Island island, Consumer<Integer> onReady, Consumer<Integer> onProgress, ScanFinishListener onFinish, Runnable onError) {
        island.setScanning(true);
        scanQueue.add(new ScanSetup(island, onReady, onProgress, onFinish, onError));
    }

    //TODO: Slowdown on large islands, to reduce CPU and I/O load, if needed
    private void scanIsland(ScanSetup setup) throws IOException, InterruptedException {
        Object lock = new Object();
        Island island = setup.island();

        //ChunkScanQueue chunkQueue = new ChunkScanQueue();

        // Scan all worlds
        List<ServerWorld> worlds = new ArrayList<>();
        worlds.add(island.getOverworld());
        if (island.hasNether) worlds.add(island.getNether());
        if (island.hasEnd) worlds.add(island.getEnd());

        long start = System.nanoTime() / 1000;

        Map<Identifier, Integer> blocks = Collections.synchronizedMap(new LinkedHashMap<>());

        Map<ServerWorld, List<Long>> toScan = new HashMap<>();
        int chunkCount = 0;

        for (ServerWorld world : worlds) {
            ThreadedAnvilChunkStorage anvilChunkStorage = world.getChunkManager().threadedAnvilChunkStorage;
            RegionBasedStorage storage = ((StorageIoWorker) anvilChunkStorage.getWorker()).storage;
            RegionBasedStorageAccessor storageAccessor = (RegionBasedStorageAccessor) (Object) storage;
            assert storageAccessor != null;

            List<Long> positions = new ArrayList<>();

            for (File file : storageAccessor.getDirectory().toFile().listFiles()) {
                RegionFile regionFile = new RegionFile(storageAccessor.getStorageKey(), file.toPath(), storageAccessor.getDirectory(), storageAccessor.getDsync());
                IntBuffer buffer = regionFile.sectorData.duplicate();
                String[] split = file.getName().split("\\.", 4);
                int regionX = Integer.parseInt(split[1]);
                int regionZ = Integer.parseInt(split[2]);

                int baseX = regionX * 32;
                int baseZ = regionZ * 32;

                for (int i = 0; i < 1024; i++) {
                    if (buffer.get(i) == 0) continue;

                    int x = i % 32;
                    int z = i / 32;

                    int chunkX = baseX + x;
                    int chunkZ = baseZ + z;

                    positions.add((long) chunkX << 32 | chunkZ);
                }

                regionFile.close();
            }

            toScan.put(world, positions);
            chunkCount += positions.size();
        }

        setup.readyListener().accept(chunkCount);

        AtomicLong lastUpdate = new AtomicLong(System.nanoTime() / 1000);
        AtomicInteger remaining = new AtomicInteger(chunkCount);
        final int finalChunkCount = chunkCount;

        List<CompletableFuture<Optional<NbtCompound>>> futures = new ArrayList<>();
        for (Map.Entry<ServerWorld, List<Long>> entry : toScan.entrySet()) {
            List<Long> positions = entry.getValue();
            ServerWorld world = entry.getKey();

            List<Long> scannedChunks = new ArrayList<>();
            ThreadedAnvilChunkStorage anvilChunkStorage = world.getChunkManager().threadedAnvilChunkStorage;
            for (ChunkHolder chunkHolder : anvilChunkStorage.chunkHolders.values().stream().filter(ChunkHolder::isAccessible).peek(ChunkHolder::updateAccessibleStatus).toList()) {
                ChunkPos pos = chunkHolder.getPos();
                if (scannedChunks.contains((long) pos.x >> 32 | pos.z)) {
                    continue;
                }
                if (!positions.contains((long) pos.x >> 32 | pos.z)) {
                    throw new RuntimeException("Chunk [%s,%s]{%s} was not in the scan queue".formatted(pos.x, pos.z, (long) pos.x >> 32 | pos.z));
                }
                scannedChunks.add((long) pos.x >> 32 | pos.z);
                positions.remove((long) pos.x >> 32 | pos.z);
                scanChunk(chunkHolder.getCurrentChunk(), blocks);

                remaining.decrementAndGet();
                long now = System.nanoTime() / 1000;
                if (now - lastUpdate.get() >= 100_000) {
                    lastUpdate.set(now);
                    int scanned = finalChunkCount - remaining.get();
                    IslandLogic.runOnNextTick(() -> setup.progressListener.accept(scanned));
                }
            }
            scannedChunks.clear();
            scannedChunks = null; //Make sure GC gets this, if it runs while this runs than this won't be wasting RAM

            for (Long position : positions) {
                int x = (int) (position >> 32);
                int z = position.intValue(); //Same as (int) (position & 0xffffffffL)
                ChunkPos pos = new ChunkPos(x, z);

                CompletableFuture<Optional<NbtCompound>> nbt = anvilChunkStorage.getNbt(pos);
                CompletableFuture<Optional<NbtCompound>> future = nbt.whenCompleteAsync((compound, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error("Error on {}, failed to get chunk data", pos);
                        return;
                    }
                    if (compound.isEmpty()) {
                        LOGGER.warn("Missing chunk data for chunk {}", pos);
                        return;
                    }

                    ProtoChunk chunk = ChunkSerializer.deserialize(world, anvilChunkStorage.getPointOfInterestStorage(), pos, compound.get());
                    scanChunk(chunk, blocks);
                    chunk = null; //Make sure GC gets this, if it runs while this runs than this won't be wasting RAM

                    int threadSafeRemaining = remaining.decrementAndGet();

                    long now = System.nanoTime() / 1000;
                    if (now - lastUpdate.get() >= 100_000) {
                        lastUpdate.set(now);
                        int scanned = finalChunkCount - threadSafeRemaining;
                        IslandLogic.runOnNextTick(() -> setup.progressListener.accept(scanned));
                    }


                    /*if (threadSafeRemaining < 300 && (threadSafeRemaining % 20 == 0 || threadSafeRemaining < 50)) {
                        System.out.println(threadSafeRemaining);
                    }*/

                    /*if (threadSafeRemaining == 0) {

                    }*/
                });
                futures.add(future);
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long end = System.nanoTime() / 1000;

        List<Map.Entry<Identifier, Integer>> entries = new LinkedList<>(blocks.entrySet());
        entries.sort(Comparator.<Map.Entry<Identifier, Integer>>comparingInt(Map.Entry::getValue).reversed());
        blocks.clear();
        entries.forEach(blockEntry -> blocks.put(blockEntry.getKey(), blockEntry.getValue()));
        setup.finishListener.finish(end - start, new LinkedHashMap<>(blocks));

        island.updateBlocks(blocks);
    }

    private static void scanChunk(Chunk chunk, Map<Identifier, Integer> blocks) {
        ExtendedChunk extendedChunk = (ExtendedChunk) chunk;
        Set<ChunkSection> nonEmptySections = extendedChunk.getNonEmptySections();
        for (ChunkSection section : nonEmptySections) {
            PalettedContainer<BlockState> stateContainer = section.getBlockStateContainer();
            stateContainer.count((blockState, amount) -> {
                if (blockState.isAir()) return;
                Identifier id = Registries.BLOCK.getId(blockState.getBlock());
                blocks.compute(id, (state, count) -> count == null ? amount : count + amount);
            });
        }
    }

    private record ScanSetup(Island island, Consumer<Integer> readyListener, Consumer<Integer> progressListener, ScanFinishListener finishListener, Runnable errorListener) {}

    @FunctionalInterface
    public interface ScanFinishListener {
        void finish(long timeTaken, Map<Identifier, Integer> scannedBlocks);
    }
}
