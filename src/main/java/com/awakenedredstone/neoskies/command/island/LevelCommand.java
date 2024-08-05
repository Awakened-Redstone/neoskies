package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.gui.PagedGui;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.mixin.accessor.FluidBlockAccessor;
import com.awakenedredstone.neoskies.util.FontUtils;
import com.awakenedredstone.neoskies.util.LinedStringBuilder;
import com.awakenedredstone.neoskies.util.MapBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.UnitConvertions;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.Block;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
            ).then(literal("top")
              .executes(context -> {
                  List<Island> top5 = IslandLogic.getInstance().islands.stuck.stream()
                    .sorted(Comparator.comparingLong(Island::getPoints))
                    .limit(5)
                    .toList().reversed();

                  LinedStringBuilder builder = new LinedStringBuilder();
                  builder.append(top5.getFirst().owner.name).append(": ").append(top5.getFirst().getPoints());
                  builder.appendLine(top5.getLast().owner.name).append(": ").append(top5.getLast().getPoints());

                  context.getSource().sendMessage(Texts.literal(builder.toString()));
                  return 0;
              })
            )
          )
        );
    }

    private static int view(ServerCommandSource source) {
        AtomicInteger sum = new AtomicInteger();

        if (!assertPlayer(source)) return 0;
        ServerPlayerEntity player = source.getPlayer();

        Island island = NeoSkiesAPI.getIslandByPlayer(player).orElse(null);
        if (!assertIsland(source, island)) return 0;
        if (island.isScanning()) {
            source.sendError(Texts.translatable("message.neoskies.island.error.scanning"));
            return 0;
        }

        List<GuiElementInterface> elements = new ArrayList<>();

        island.getBlocks().forEach((blockId, count) -> {
            Integer points = IslandLogic.getRankingConfig().getPoints(blockId);
            sum.addAndGet(points * count);

            Block block = Registries.BLOCK.get(blockId);
            ItemStack stack;
            if (block instanceof FluidBlockAccessor fluid) {
                stack = fluid.getFluid().getBucketItem().getDefaultStack();
            } else {
                ItemStack item = block.asItem().getDefaultStack();
                if (!item.isEmpty()) {
                    stack = item;
                } else {
                    stack = block.getPickStack(island.getOverworld(), BlockPos.ORIGIN, block.getDefaultState());
                }
            }
            GuiElementBuilder builder = GuiElementBuilder.from(stack.isEmpty() ? new ItemStack(Items.BARRIER) : stack)
              .hideDefaultTooltip()
              .addLoreLine(Text.empty())
              //TODO: multiline
              .addLoreLine(Texts.loreBase("gui.neoskies.level.item.description", placeholders -> {
                  placeholders.put("points", String.valueOf(points * count));
                  placeholders.put("count", String.valueOf(count));
              }));

            builder.setName(block.getName());
            if (stack.isEmpty()) {
                //TODO: color
                builder.setName(block.getName().formatted(Formatting.WHITE));
            }
            elements.add(builder.build());
        });

        SimpleGui gui = PagedGui.of(player, elements);
        gui.setTitle(Texts.translatable("gui.neoskies.level.title", map -> {
            map.put("points", String.valueOf(island.getPoints()));
            map.put("level", String.valueOf(island.getLevel()));
        }));
        gui.open();

        source.sendFeedback(() -> Texts.translatable(sum.get() + " points"), false);

        return sum.get();
    }

    private static int runScan(ServerCommandSource source) {
        if (!assertPlayer(source)) return 0;
        ServerPlayerEntity player = source.getPlayer();

        Island island = NeoSkiesAPI.getIslandByPlayer(player).orElse(null);
        if (!assertIsland(source, island)) return 0;
        if (island.isScanning()) {
            source.sendError(Texts.translatable("message.neoskies.island.error.scanning"));
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
        Identifier scanCloseTaskId = Identifier.of(island.getIslandId().toString(), "close_scan_screen");

        MutableText text = Texts.translatable("message.neoskies.island.level.scan.background").copy();

        Text startScanText = Texts.translatable("message.neoskies.island.level.scan.start");
        Text cancelText = Texts.translatable("message.neoskies.island.level.scan.cancel");

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

            IslandLogic.getInstance().scheduler.scheduleDelayed(IslandLogic.getServer(), 8, () -> {
                textDisplay.setInterpolationDuration(7);
                textDisplay.setScale(new Vec3d(0, 0.1, 1).toVector3f());
                textDisplay.startInterpolation();
            });

            IslandLogic.getInstance().scheduler.scheduleDelayed(IslandLogic.getServer(), 15, () -> {
                holder.destroy();
                holder = null;
            });
        };

        AtomicReference<TextDisplayElement> message = new AtomicReference<>();
        AtomicReference<Pair<TextDisplayElement, Set<InteractionElement>>> startScanPair = new AtomicReference<>();
        AtomicReference<Pair<TextDisplayElement, Set<InteractionElement>>> cancalScanPair = new AtomicReference<>();

        //TODO: Fix all 100 bugs this thing has
        //TODO: Make this readable
        TextDisplayElement display = createDisplay(Texts.literal(""), yaw, new Vec3d(0, 0, 0));
        VirtualElement.InteractionHandler startScan = createHandler((interactor, hand) -> {
            if (island.isScanning()) {
                source.sendError(Texts.translatable("message.neoskies.island.error.scanning"));
                return;
            }

            removeInteraction(startScanPair.get());
            removeInteraction(cancalScanPair.get());
            removeDisplay(message.get());

            IslandLogic.getScheduler().scheduleDelayed(scanCloseTaskId, IslandLogic.getServer(), 0, () -> {});

            display.setTranslation(new Vec3d(0, 0.25 * (lines / 2d) + 0.0625 + 0.5, 0).toVector3f());
            display.setText(Texts.translatable("message.neoskies.island.level.scan.preparing"));

            AtomicInteger toScan = new AtomicInteger();
            IslandLogic.getInstance().islandScanner.queueScan(island, total -> {
                toScan.set(total);
                display.setText(Texts.translatable("message.neoskies.island.level.scan.progress", new MapBuilder.StringMap()
                  .putAny("progress", 0)
                  .putAny("total", UnitConvertions.readableNumber(total))
                  .build()));
            }, current -> {
                int total = toScan.get();
                Text progress = Texts.translatable("message.neoskies.island.level.scan.progress", new MapBuilder.StringMap()
                  .putAny("progress", UnitConvertions.readableNumber(current))
                  .putAny("total", UnitConvertions.readableNumber(total))
                  .build());
                IslandLogic.getInstance().scheduler.schedule(new Identifier("neoskies", "island-scan/" + island.getIslandId().toString()), 0, () -> display.setText(progress));
            }, (timeTaken, scannedBlocks) -> {
                IslandLogic.runOnNextTick(() -> {
                    int scanned = scannedBlocks.values().stream().mapToInt(value -> value).sum();

                    source.sendFeedback(() -> Texts.translatable("message.neoskies.island.level.scan.time_taken", new MapBuilder.StringMap()
                      .put("time", UnitConvertions.formatTimings(timeTaken))
                      .putAny("count", UnitConvertions.readableNumber(scanned))
                      .build()), false);
                    removeDisplay(display);
                    float width = getTextWidth(text);
                    List<BlockDisplayElement> blockDisplays = new ArrayList<>();
                    List<TextDisplayElement> labels = new ArrayList<>();
                    List<Map.Entry<Identifier, Integer>> list = scannedBlocks.entrySet().stream().toList();
                    for (int i = 0; i < Math.min(8, list.size()); i++) {
                        Map.Entry<Identifier, Integer> entry = list.get(i);
                        Identifier id = entry.getKey();
                        Integer amount = entry.getValue();

                        Block block = Registries.BLOCK.get(id);
                        BlockDisplayElement blockDisplay = new BlockDisplayElement();
                        blockDisplay.setBlockState(block.getDefaultState());
                        blockDisplay.setYaw(yaw + 180);
                        blockDisplay.setScale(new Vec3d(0.25, 0.25, 0.01).toVector3f());
                        blockDisplay.setTranslation(new Vec3d(-width + 0.3, 0.25 * (lines) + 0.0625 - i * 0.3, 0).toVector3f());
                        blockDisplay.setBrightness(Brightness.FULL);
                        holder.addElement(blockDisplay);
                        blockDisplays.add(blockDisplay);
                        Text amountText = Texts.translatable("message.neoskies.island.level.scan.block_info", new MapBuilder.StringMap()
                          .putAny("amount", UnitConvertions.readableNumber(amount))
                          .put("block", block.getName().getString())
                          .build());
                        labels.add(createDisplay(amountText, yaw, new Vec3d(-width + 0.7 + (getTextWidth(amountText)), 0.25 * (lines) + 0.0625 - i * 0.3 - 0.03125, 0)));
                    }

                    var ref = new Object() {
                        Pair<TextDisplayElement, Set<InteractionElement>> closeButton = null;
                    };
                    Runnable removeBlocksView = () -> {
                        blockDisplays.forEach(holder::removeElement);
                        labels.forEach(holder::removeElement);
                        removeInteraction(ref.closeButton);
                    };

                    ref.closeButton = createInteraction(Texts.translatable("message.neoskies.island.level.scan.close"), createHandler((player1, hand1) -> {
                        removeBlocksView.run();
                        closeBackground.run();
                    }), yaw);

                    IslandLogic.getScheduler().scheduleDelayed(scanCloseTaskId, IslandLogic.getServer(), 600, () -> {
                        if (holder == null) return;
                        removeBlocksView.run();
                        closeBackground.run();
                    });
                });
            }, () -> {
                display.setText(Texts.translatable("message.neoskies.island.level.scan.error"));
            });
        });

        VirtualElement.InteractionHandler cancelScan = createHandler((interactor, hand) -> {
            removeDisplay(message.get());
            removeInteraction(startScanPair.get());
            removeInteraction(cancalScanPair.get());
            closeBackground.run();

            IslandLogic.getScheduler().scheduleDelayed(scanCloseTaskId, IslandLogic.getServer(), 0, () -> {});
        });

        textDisplay.setInterpolationDuration(7);
        IslandLogic.getScheduler().scheduleDelayed(IslandLogic.getServer(), 0, () -> {
            textDisplay.setScale(new Vec3d(1, 0.1, 1).toVector3f());
            textDisplay.startInterpolation();
        });

        IslandLogic.getScheduler().scheduleDelayed(IslandLogic.getServer(), 10, () -> {
            textDisplay.setInterpolationDuration(5);
            textDisplay.setScale(new Vec3d(1, 1, 1).toVector3f());
            textDisplay.setTranslation(new Vec3d(0, 0, 0).toVector3f());
            textDisplay.startInterpolation();
        });

        IslandLogic.getScheduler().scheduleDelayed(IslandLogic.getServer(), 15, () -> {
            message.set(createDisplay(Texts.translatable("message.neoskies.island.level.scan.confirm"), yaw, new Vec3d(0, 0.25 * (lines / 2d) + 0.0625 + 0.5, 0)));
            startScanPair.set(createInteraction(startScanText, startScan, yaw, new Vec3d(-2 + getTextWidth(startScanText), 0.25 * (lines / 2d) + 0.0625, 0)));
            cancalScanPair.set(createInteraction(cancelText, cancelScan, yaw, new Vec3d(2 - getTextWidth(startScanText), 0.25 * (lines / 2d) + 0.0625, 0)));
        });

        IslandLogic.getScheduler().scheduleDelayed(scanCloseTaskId, IslandLogic.getServer(), 600, () -> {
            if (holder != null && holder.getElements().contains(message.get())) {
                removeDisplay(message.get());
                removeInteraction(startScanPair.get());
                removeInteraction(cancalScanPair.get());
                closeBackground.run();
            }
        });
        source.sendFeedback(() -> Texts.translatable("message.neoskies.island.level.scan.opening"), false);
        return 1;
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
