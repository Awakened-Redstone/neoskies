package com.awakenedredstone.neoskies.logic.level;

import com.awakenedredstone.neoskies.duck.ExtendedChunk;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.logic.Member;
import com.awakenedredstone.neoskies.mixin.accessor.RegionBasedStorageAccessor;
import com.awakenedredstone.neoskies.util.Texts;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.parser.ParseException;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.RegionFile;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.StorageKey;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class IslandScanner implements AutoCloseable {
    public static final Logger LOGGER = LoggerFactory.getLogger("Island Scanner");
    private final BlockingQueue<ScanSetup> scanQueue = new LinkedBlockingQueue<>();
    private final ExecutorService queueExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("Island-Scan-Queue-Worker");
        thread.setDaemon(true); // Allow server to shut down even if scanning is in progress
        thread.setUncaughtExceptionHandler((t, e) -> {
            CrashReport crashReport = CrashReport.create(e, "Unhandled exception on island scan");
            throw new CrashException(crashReport);
        });
        return thread;
    });
    private int threadCount = 0;
    private final ExecutorService scanExecutor = Executors.newFixedThreadPool(IslandLogic.getConfig().islandScan.chunkCores, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("Island-Scan-Worker-" + threadCount++);
        thread.setDaemon(true); // Allow server to shut down even if scanning is in progress
        thread.setUncaughtExceptionHandler((t, e) -> {
            CrashReport crashReport = CrashReport.create(e, "Unhandled exception on island scan");
            throw new CrashException(crashReport);
        });
        return thread;
    });

    public IslandScanner() {
        queueExecutor.submit(() -> {
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

                    IslandLogic.scheduleDelayed(10, System::gc);
                    islandToScan.island().setScanning(false);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void close() {
        queueExecutor.shutdownNow();
    }

    public void queueScan(@NotNull Island island, Consumer<Integer> onReady, Consumer<Integer> onProgress, ScanFinishListener onFinish, Runnable onError) {
        island.setScanning(true);
        scanQueue.add(new ScanSetup(island, onReady, onProgress, onFinish, onError));
    }

    private void scanIsland(ScanSetup setup) throws IOException, InterruptedException {
        AtomicBoolean failed = new AtomicBoolean();
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

                    if (x + z * 32 != i) {
                        throw new RuntimeException("Parsed pos wrong");
                    }

                    int chunkX = baseX + x;
                    int chunkZ = baseZ + z;

                    long pos = (long) chunkX << 32 | (long) chunkZ << 32 >>> 32;

                    if (positions.contains(pos)) {
                        LOGGER.error("Chunk [{}, {} | {}, {}] ({}, {}) ({}) ({}) ({}) added to queue twice!", chunkX, chunkZ, x, z, regionX, regionZ, file.getName(), pos, world.getDimensionEntry().getIdAsString());
                        throw new RuntimeException("Chunk added to queue twice!");
                    }

                    positions.add(pos);
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

        PointOfInterestStorage poiStorage = new PointOfInterestStorage(new StorageKey("", null, ""), null, null, false, null, null) {
            @Override
            public void initForPalette(ChunkSectionPos sectionPos, ChunkSection chunkSection) { }
        };

        //TODO: make threads scan regions instead of a single chunk
        List<CompletableFuture<Void>> futures = Collections.synchronizedList(new ArrayList<>());
        for (Map.Entry<ServerWorld, List<Long>> entry : toScan.entrySet()) {
            List<Long> positions = entry.getValue();
            ServerWorld world = entry.getKey();

            List<Long> scannedChunks = new ArrayList<>();
            ThreadedAnvilChunkStorage anvilChunkStorage = world.getChunkManager().threadedAnvilChunkStorage;
            for (ChunkHolder chunkHolder : anvilChunkStorage.chunkHolders.values().stream().filter(ChunkHolder::isAccessible).peek(ChunkHolder::updateAccessibleStatus).toList()) {
                ChunkPos pos = chunkHolder.getPos();
                if (scannedChunks.contains((long) pos.x >> 32 | (long) pos.z << 32 >>> 32)) {
                    continue;
                }
                if (!positions.contains((long) pos.x >> 32 | (long) pos.z << 32 >>> 32)) {
                    throw new RuntimeException("Chunk [%s,%s]{%s} was not in the scan queue".formatted(pos.x, pos.z, (long) pos.x >> 32 | pos.z));
                }
                scannedChunks.add((long) pos.x >> 32 | (long) pos.z << 32 >>> 32);
                positions.remove((long) pos.x >> 32 | (long) pos.z << 32 >>> 32);
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

            for (Long position : positions) {
                int x = (int) (position >> 32);
                int z = position.intValue(); //Same as (int) (position & 0xffffffffL)
                ChunkPos pos = new ChunkPos(x, z);

                CompletableFuture<Void> future = new CompletableFuture<>();
                futures.add(future);

                CompletableFuture<Optional<NbtCompound>> nbt = anvilChunkStorage.getNbt(pos);
                scanExecutor.submit(() -> {
                    Optional<NbtCompound> compound;
                    try {
                        compound = nbt.join();
                    } catch (Exception e) {
                        LOGGER.error("Error on {}, failed to get chunk data", pos, e);
                        return;
                    }

                    if (failed.get()) {
                        future.complete(null);
                        return;
                    }
                    if (scannedChunks.contains(position)) {
                        LOGGER.error("Tried to scan {} twice, giving up!", pos);
                        throw new RuntimeException("Scanned chunk twice");
                    }
                    scannedChunks.add(position);
                    if (compound.isEmpty()) {
                        LOGGER.warn("Missing chunk data for chunk {}", pos);
                        return;
                    }

                    NbtCompound nbtData = compound.get();

                    try {
                        if (failed.get()) {
                            future.complete(null);
                            return;
                        }
                        ProtoChunk chunk = ChunkSerializer.deserialize(world, poiStorage, pos, nbtData);
                        if (failed.get()) {
                            future.complete(null);
                            return;
                        }
                        scanChunk(chunk, blocks);
                        chunk = null; //Make sure GC can get this earlier, if it runs while this runs than this won't be wasting RAM
                    } catch (Throwable e) {
                        LOGGER.error("Failed to deserialize chunk {} with data of size {}", pos, nbtData.getSize());
                        failed.set(true);
                        future.completeExceptionally(e);
                        throw e;
                    }

                    if (failed.get()) {
                        future.complete(null);
                        return;
                    }
                    int threadSafeRemaining = remaining.decrementAndGet();

                    long now = System.nanoTime() / 1000;
                    if (now - lastUpdate.get() >= 100_000) {
                        if (failed.get()) {
                            future.complete(null);
                            return;
                        }
                        lastUpdate.set(now);
                        int scanned = finalChunkCount - threadSafeRemaining;
                        IslandLogic.runOnNextTick(() -> {
                            if (failed.get()) return;
                            setup.progressListener.accept(scanned);
                        });
                    }

                    future.complete(null);
                });
            }
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Throwable e) {
            failed.set(true);
            LOGGER.error("Scan crashed", e);
            throw new RuntimeException(e);
        } finally {
            poiStorage.close();
            futures.clear();
        }

        long end = System.nanoTime() / 1000;

        List<Map.Entry<Identifier, Integer>> entries = new LinkedList<>(blocks.entrySet());
        entries.sort(Comparator.<Map.Entry<Identifier, Integer>>comparingInt(Map.Entry::getValue).reversed());
        blocks.clear();
        entries.forEach(blockEntry -> blocks.put(blockEntry.getKey(), blockEntry.getValue()));
        IslandLogic.runOnNextTick(() -> setup.finishListener.finish(end - start, new LinkedHashMap<>(blocks)));

        try {
            island.updateBlocks(blocks);
        } catch (EvaluationException | ParseException e) {
            for (Member member : island.getAllMembers()) {
                ServerPlayerEntity player = IslandLogic.getServer().getPlayerManager().getPlayer(member.uuid);
                if (player != null) {
                    player.sendMessage(Texts.translatable("commands.neoskies.level.scan.error.update"));
                }
            }
        }
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

    private record ScanSetup(Island island, Consumer<Integer> readyListener, Consumer<Integer> progressListener, ScanFinishListener finishListener, Runnable errorListener) { }

    @FunctionalInterface
    public interface ScanFinishListener {
        void finish(long timeTaken, Map<Identifier, Integer> scannedBlocks);
    }
}
