package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.gui.PagedGui;
import com.awakenedredstone.neoskies.util.MapBuilder;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import com.awakenedredstone.neoskies.SkylandsMain;
import com.awakenedredstone.neoskies.api.SkylandsAPI;
import com.awakenedredstone.neoskies.duck.ExtendedChunk;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.Skylands;
import com.awakenedredstone.neoskies.logic.util.ChunkScanQueue;
import com.awakenedredstone.neoskies.util.FontUtils;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.UnitConvertions;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.assertIsland;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.assertPlayer;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.requiresIsland;
import static net.minecraft.server.command.CommandManager.literal;

public class LevelCommand {
    private static ElementHolder holder;

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
          .then(literal("level")
            .requires(requiresIsland("neoskies.command.level", true))
            .then(literal("scan")
              .executes(context -> LevelCommand.runScan(context.getSource()))
            ).then(literal("view")
              .executes(context -> LevelCommand.view(context.getSource()))
            )
          )
        );
    }

    private static int view(ServerCommandSource source) {
        AtomicInteger sum = new AtomicInteger();

        if (!assertPlayer(source)) return 0;
        ServerPlayerEntity player = source.getPlayer();

        Island island = SkylandsAPI.getIslandByPlayer(player).orElse(null);
        if (!assertIsland(source, island)) return 0;
        if (island.isScanning()) {
            source.sendError(Texts.of("message.neoskies.island.error.scanning"));
            return 0;
        }

        List<GuiElementInterface> elements = new ArrayList<>();

        island.getBlocks().forEach((block, count) -> {
            Integer points = Skylands.getRankingConfig().points.getOrDefault(block, 1);
            sum.addAndGet(points * count);

            Block block1 = Registries.BLOCK.get(block);
            ItemStack stack = block1.asItem().getDefaultStack();
            GuiElementBuilder builder = GuiElementBuilder.from(stack.isEmpty() ? new ItemStack(Items.BARRIER) : stack)
              .hideFlags()
              .addLoreLine(Texts.of("x%d blocks".formatted(count)))
              .addLoreLine(Texts.of("%d points".formatted(points * count)));

            if (stack.isEmpty()) {
                builder.setName(block1.getName());
            }
            elements.add(builder.build());
        });

        SimpleGui gui = PagedGui.of(player, elements);
        gui.setTitle(Texts.of("Island points"));
        gui.open();

        source.sendFeedback(() -> Texts.of(sum.get() + " points"), false);

        return sum.get();
    }

    private static int runScan(ServerCommandSource source) {
        if (!assertPlayer(source)) return 0;
        ServerPlayerEntity player = source.getPlayer();

        Island island = SkylandsAPI.getIslandByPlayer(player).orElse(null);
        if (!assertIsland(source, island)) return 0;
        if (island.isScanning()) {
            source.sendError(Texts.of("message.neoskies.island.error.scanning"));
            return 0;
        }

        BlockPos blockPos = player.getBlockPos();
        World world = player.getWorld();

        Vec3d pos = player.getPos();
        Vec3d look = player.getRotationVector();
        Vec3d lookVec = new Vec3d(look.x, 0, look.z).normalize();
        pos = pos.add(lookVec.multiply(2.5)).add(0, 0, 0);

        float yaw = player.getYaw();

        if (holder != null) holder.destroy();
        holder = new ElementHolder();
        new ChunkAttachment(holder, (WorldChunk) world.getChunk(blockPos), pos, true);

        MutableText text = Texts.of("message.neoskies.island.level.scan.background").copy();

        Text startScanText = Texts.of("message.neoskies.island.level.scan.start");
        Text cancelText = Texts.of("message.neoskies.island.level.scan.cancel");

        TextDisplayElement textDisplay = new TextDisplayElement();
        textDisplay.setText(text);
        int lines = textDisplay.getText().getString().lines().toList().size();
        textDisplay.setYaw(yaw + 180);
        textDisplay.setScale(new Vec3d(0, 0.1, 1).toVector3f());
        textDisplay.setTranslation(new Vec3d(0, 0.25 * (lines / 2d) + 0.0625, 0).toVector3f());
        textDisplay.setBrightness(Brightness.FULL);
        holder.addElement(textDisplay);

        Runnable closeBackground = () -> {
            textDisplay.setInterpolationDuration(5);
            textDisplay.setScale(new Vec3d(1, 0.1, 1).toVector3f());
            textDisplay.setTranslation(new Vec3d(0, 0.25 * (lines / 2d) + 0.0625, 0).toVector3f());
            textDisplay.startInterpolation();

            Skylands.getInstance().scheduler.scheduleDelayed(Skylands.getServer(), 8, () -> {
                textDisplay.setInterpolationDuration(7);
                textDisplay.setScale(new Vec3d(0, 0.1, 1).toVector3f());
                textDisplay.startInterpolation();
            });

            Skylands.getInstance().scheduler.scheduleDelayed(Skylands.getServer(), 15, () -> {
                holder.destroy();
                holder = null;
            });
        };

        AtomicReference<TextDisplayElement> message = new AtomicReference<>();
        AtomicReference<Pair<TextDisplayElement, Set<InteractionElement>>> startScanPair = new AtomicReference<>();
        AtomicReference<Pair<TextDisplayElement, Set<InteractionElement>>> cancalScanPair = new AtomicReference<>();

        TextDisplayElement display = createDisplay(Texts.of(""), yaw, new Vec3d(0, 0, 0));
        VirtualElement.InteractionHandler startScan = createHandler((interactor, hand) -> {
            if (island.isScanning()) {
                source.sendError(Texts.of("message.neoskies.island.level.scan.running"));
                return;
            }
            scanChunks(island, () -> {
                removeInteraction(startScanPair.get());
                removeInteraction(cancalScanPair.get());
                removeDisplay(message.get());

                display.setTranslation(new Vec3d(0, 0.25 * (lines / 2d) + 0.0625 + 0.5, 0).toVector3f());
                display.setText(Texts.of("message.neoskies.island.level.scan.preparing"));
            }, total -> {
                if (total > 200) {
                    display.setText(Texts.of("message.neoskies.island.level.scan.progress", new MapBuilder.StringMap()
                      .putAny("progress", 0)
                      .putAny("total", total)
                      .build()));
                    return;
                }
                MutableText visualization = Text.empty();
                int size = (int) Math.sqrt(total);
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        visualization.append(Texts.of("<gray>█"));
                    }
                    visualization.append(Text.of("\n"));
                }
                display.setText(visualization);
                //charsWidth * scale = 3; charsWidth = (9/80d) * size; scale = ?; so I must calculate the scale
                double scale = 1.5 / ((9 / 80d) * size);
                display.setTranslation(new Vec3d(0, -(9 / 35d) * scale, 0).toVector3f());
                display.setScale(new Vec3d(scale, scale, 1).toVector3f());
            }, (total, current) -> {
                if (total > 200) {
                    Text progress = Texts.of("message.neoskies.island.level.scan.progress", new MapBuilder.StringMap()
                      .putAny("progress", current)
                      .putAny("total", total)
                      .build());
                    Skylands.getInstance().scheduler.schedule(new Identifier("neoskies", "island-scan/" + island.getIslandId().toString()), 0, () -> display.setText(progress));
                    return;
                }

                MutableText visualization = Text.empty();
                int size = (int) Math.sqrt(total);
                int index = 0;
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        visualization.append(Texts.of(index < current ? "<green>█" : index == current ? "<yellow>█" : "<gray>█"));
                        index++;
                    }
                    visualization.append(Text.of("\n"));
                }
                Skylands.getInstance().scheduler.schedule(new Identifier("neoskies", "island-scan/" + island.getIslandId().toString()), 0, () -> display.setText(visualization));
            }, (timeTaken, scannedBlocks) -> {
                Skylands.syncWithTick(() -> {
                    source.sendFeedback(() -> Texts.of("message.neoskies.island.level.scan.time_taken", new MapBuilder.StringMap()
                      .put("time", UnitConvertions.formatTimings(timeTaken))
                      .build()), false);
                    removeDisplay(display);
                    float width = getTextWidth(text);
                    AtomicInteger i = new AtomicInteger();
                    List<BlockDisplayElement> blockDisplays = new ArrayList<>();
                    List<TextDisplayElement> labels = new ArrayList<>();
                    scannedBlocks.forEach((id, amount) -> {
                        if (i.get() > 8) return;
                        Block block = Registries.BLOCK.get(id);
                        BlockDisplayElement blockDisplay = new BlockDisplayElement();
                        blockDisplay.setBlockState(block.getDefaultState());
                        blockDisplay.setYaw(yaw + 180);
                        blockDisplay.setScale(new Vec3d(0.25, 0.25, 0.01).toVector3f());
                        blockDisplay.setTranslation(new Vec3d(-width + 0.3, 0.25 * (lines) + 0.0625 - (i.get()) * 0.3, 0).toVector3f());
                        blockDisplay.setBrightness(Brightness.FULL);
                        holder.addElement(blockDisplay);
                        blockDisplays.add(blockDisplay);
                        Text amountText = Texts.of("message.neoskies.island.level.scan.block_info", new MapBuilder.StringMap()
                          .putAny("amount", amount)
                          .put("block", block.getName().getString())
                          .build());
                        labels.add(createDisplay(amountText, yaw, new Vec3d(-width + 0.7 + (getTextWidth(amountText)), 0.25 * (lines) + 0.0625 - (i.get()) * 0.3 - 0.03125, 0)));
                        i.getAndIncrement();
                    });

                    var ref = new Object() {
                        Pair<TextDisplayElement, Set<InteractionElement>> closeButton = null;
                    };
                    Runnable removeBlocksView = () -> {
                        blockDisplays.forEach(holder::removeElement);
                        labels.forEach(holder::removeElement);
                        removeInteraction(ref.closeButton);
                    };

                    ref.closeButton = createInteraction(Texts.of("message.neoskies.island.level.scan.close"), createHandler((player1, hand1) -> {
                        removeBlocksView.run();
                        closeBackground.run();
                    }), yaw);

                    Skylands.getScheduler().scheduleDelayed(Skylands.getServer(), 600, () -> {
                        if (holder == null) return;
                        removeBlocksView.run();
                        closeBackground.run();
                    });
                });
            });
        });

        VirtualElement.InteractionHandler cancelScan = createHandler((interactor, hand) -> {
            removeDisplay(message.get());
            removeInteraction(startScanPair.get());
            removeInteraction(cancalScanPair.get());
            closeBackground.run();
        });

        textDisplay.setInterpolationDuration(7);
        Skylands.getInstance().scheduler.scheduleDelayed(Skylands.getServer(), 0, () -> {
            textDisplay.setScale(new Vec3d(1, 0.1, 1).toVector3f());
            textDisplay.startInterpolation();
        });

        Skylands.getInstance().scheduler.scheduleDelayed(Skylands.getServer(), 10, () -> {
            textDisplay.setInterpolationDuration(5);
            textDisplay.setScale(new Vec3d(1, 1, 1).toVector3f());
            textDisplay.setTranslation(new Vec3d(0, 0, 0).toVector3f());
            textDisplay.startInterpolation();
        });

        Skylands.getInstance().scheduler.scheduleDelayed(Skylands.getServer(), 15, () -> {
            message.set(createDisplay(Texts.of("message.neoskies.island.level.scan.confirm"), yaw, new Vec3d(0, 0.25 * (lines / 2d) + 0.0625 + 0.5, 0)));
            startScanPair.set(createInteraction(startScanText, startScan, yaw, new Vec3d(-2 + getTextWidth(startScanText), 0.25 * (lines / 2d) + 0.0625, 0)));
            cancalScanPair.set(createInteraction(cancelText, cancelScan, yaw, new Vec3d(2 - getTextWidth(startScanText), 0.25 * (lines / 2d) + 0.0625, 0)));
        });

        Skylands.getScheduler().scheduleDelayed(Skylands.getServer(), 600, () -> {
            if (holder != null && holder.getElements().contains(message.get())) {
                removeDisplay(message.get());
                removeInteraction(startScanPair.get());
                removeInteraction(cancalScanPair.get());
                closeBackground.run();
            }
        });
        source.sendFeedback(() -> Texts.of("message.neoskies.island.level.scan.opening"), false);
        return 1;
    }

    private static void scanChunks(Island island, Runnable preparing, Consumer<Integer> informTotal, BiConsumer<Integer, Integer> updater, ScanFinish finisher) {
        Thread scanThread = new Thread(() -> {
            island.setScanning(true);
            ChunkScanQueue chunks = new ChunkScanQueue();
            int radius = island.radius;
            preparing.run();
            ServerWorld world = island.getOverworld();

            //for islands below 200 radius we can use a slower process that looks cool, otherwise we have to use the faster process at the cost of the graph
            if (radius <= 200 && radius > 0) {
                //get all chunks in radius, the island center always is at 0, 0
                BlockPos center = new BlockPos(0, 0, 0);
                for (int x = -radius; x <= radius; x += 16) {
                    for (int z = -radius; z <= radius; z += 16) {
                        BlockPos pos = new BlockPos(x, 0, z);
                        BlockPos blockPos = center.add(pos);
                        WorldChunk chunk = (WorldChunk) world.getChunk(blockPos);
                        if (chunk != null) {
                            chunks.add(chunk);
                        }
                    }
                }
            } else {
                ThreadedAnvilChunkStorage anvilChunkStorage = world.getChunkManager().threadedAnvilChunkStorage;
                List<ChunkHolder> chunkHolders = anvilChunkStorage.chunkHolders.values().stream().filter(ChunkHolder::isAccessible).peek(ChunkHolder::updateAccessibleStatus).toList();
                chunkHolders.forEach(chunkHolder -> chunks.add(chunkHolder.getCurrentChunk()));
            }

            informTotal.accept(chunks.size());

            //scan all chunks
            LinkedHashMap<Identifier, Integer> blocks = new LinkedHashMap<>();
            long start = System.nanoTime() / 1000;
            while (!chunks.finished()) {
                updater.accept(chunks.size(), chunks.getPos());
                Chunk chunk = chunks.poll();
                Set<ChunkSection> nonEmptySections = ((ExtendedChunk) chunk).getNonEmptySections();
                for (ChunkSection section : nonEmptySections) {
                    PalettedContainer<BlockState> stateContainer = section.getBlockStateContainer();
                    stateContainer.count((blockState, amount) -> {
                        if (blockState.isAir()) return;
                        Identifier id = Registries.BLOCK.getId(blockState.getBlock());
                        blocks.compute(id, (state, count) -> count == null ? amount : count + amount);
                    });
                }
            }

            updater.accept(chunks.size(), chunks.getPos());
            long end = System.nanoTime() / 1000;

            List<Map.Entry<Identifier, Integer>> entries = new LinkedList<>(blocks.entrySet());
            entries.sort(Comparator.comparingInt(Map.Entry::getValue));
            Collections.reverse(entries);
            blocks.clear();
            entries.forEach(entry -> blocks.put(entry.getKey(), entry.getValue()));
            finisher.finish(end - start, blocks);

            island.updateBlocks(blocks);

            island.setScanning(false);
        });
        scanThread.setDaemon(true);
        scanThread.setName(island.owner.name + "'s Island Scan");
        scanThread.start();
    }

    private static float getTextWidth(Text text) {
        //Horizontal scale factor -> 1/80 | Vertical scale factor 1/4 | Calculate in double for better precision
        return (float) (FontUtils.getStringWidth(text.getString()) * (1 / 80d));
    }

    private static TextDisplayElement createDisplay(Text message, float yaw, @Nullable Vec3d offset) {
        TextDisplayElement textDisplay = new TextDisplayElement();
        textDisplay.setText(message);
        textDisplay.setYaw(yaw + 180);
        if (offset != null) textDisplay.setTranslation(offset.add(0, 0, 0).toVector3f());
        textDisplay.setBrightness(Brightness.FULL);
        textDisplay.setDefaultBackground(false);
        textDisplay.setBackground(0x00000000);
        holder.addElement(textDisplay);
        return textDisplay;
    }

    private static Pair<TextDisplayElement, Set<InteractionElement>> createInteraction(Text message, VirtualElement.InteractionHandler handler, float yaw) {
        return createInteraction(message, handler, yaw, null);
    }

    private static Pair<TextDisplayElement, Set<InteractionElement>> createInteraction(Text message, VirtualElement.InteractionHandler handler, float yaw, @Nullable Vec3d offset) {
        TextDisplayElement textDisplay = new TextDisplayElement();
        textDisplay.setText(message);
        textDisplay.setYaw(yaw + 180);
        textDisplay.setBrightness(Brightness.FULL);
        if (offset != null) textDisplay.setTranslation(offset.add(0, 0, 0).toVector3f());
        textDisplay.setDefaultBackground(false);
        textDisplay.setBackground(0x00000000);
        holder.addElement(textDisplay);

        double textOffset = getTextWidth(message);
        InteractionElement interaction;
        Set<InteractionElement> interactions = new HashSet<>();
        if (offset == null) offset = new Vec3d(0, 0, 0);
        for (double i = 0.0; i < textOffset * 2; i += 0.125f) {
            interaction = new InteractionElement();
            interaction.setHandler(handler);
            interaction.setWidth(0.125f);
            interaction.setHeight(9 * 0.03f);
            interaction.setOffset(offset.multiply(-1, 1, 1).add(i - textOffset, 0, 0).rotateY((float) Math.toRadians(-yaw)));
            holder.addElement(interaction);
            interactions.add(interaction);
        }

        return new Pair<>(textDisplay, interactions);
    }

    private static VirtualElement.InteractionHandler createHandler(Interaction handler) {
        return new VirtualElement.InteractionHandler() {
            @Override
            public void interact(ServerPlayerEntity player, Hand hand) {
                handler.interact(player, hand);
            }

            @Override
            public void attack(ServerPlayerEntity player) {
                handler.interact(player, null);
            }
        };
    }

    private static void removeDisplay(TextDisplayElement display) {
        if (display == null) return;
        holder.removeElement(display);
    }

    private static void removeInteraction(Pair<TextDisplayElement, Set<InteractionElement>> interaction) {
        if (interaction == null) return;
        holder.removeElement(interaction.getLeft());
        interaction.getRight().forEach(holder::removeElement);
    }

    @FunctionalInterface
    interface Interaction {
        void interact(ServerPlayerEntity player, @Nullable Hand hand);
    }

    @FunctionalInterface
    interface ScanFinish {
        void finish(long timeTaken, Map<Identifier, Integer> scannedBlocks);
    }
}
