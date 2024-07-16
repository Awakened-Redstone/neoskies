package com.awakenedredstone.neoskies.test;

import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.MapBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.UnitConvertions;
import dev.deftu.imgui.ImGuiRenderer;
import imgui.ImGui;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.extension.implot.flag.ImPlotStyleVar;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DebugImGuiRenderer implements ImGuiRenderer {
    private static final ImPlotContext IMPLOT_CONTEXT;
    public static final ImBoolean OPEN = new ImBoolean(false);
    private static final Map<String, Text> MESSAGES = new HashMap<>();

    static {
        IMPLOT_CONTEXT = ImPlot.createContext();
    }

    public static int bufferLength = 600;
    public static final List<Long> MEMORY = new LinkedList<>() {
        @Override
        public boolean add(Long aFloat) {
            if (this.size() >= 10000) {
                this.removeFirst();
            }
            return super.add(aFloat);
        }
    };

    @Override
    public void render() {
        if (!OPEN.get()) return;

        /*ImPlot.showDemoWindow(OPEN);
        ImGui.showDemoWindow();*/

        ImGui.setNextWindowSize(600, 750, ImGuiCond.FirstUseEver);
        if (ImGui.begin("NeoSkies debug tools", OPEN, ImGuiWindowFlags.MenuBar)) {
            if (MinecraftClient.getInstance().player != null && IslandLogic.getInstance() != null) {
                ImGui.text(IslandLogic.getServer().getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid()).getWorld().getRegistryKey().getValue().toString());
                ImGui.text(String.valueOf(MinecraftClient.getInstance().player.getWorld().getWorldBorder().getSize()));
            }

            if (ImGui.collapsingHeader("Server RAM")) {
                final Float[] samples = new Float[bufferLength];
                Arrays.fill(samples, 0f);

                final Float[] samples2 = new Float[bufferLength];
                for (int i = 0; i < samples.length; i++) {
                    try {
                        if (MEMORY.size() < samples.length && i < MEMORY.size()) {
                            samples[samples.length - MEMORY.size() + i] = (float) toMiB(MEMORY.get(i));
                        } else if (i < MEMORY.size()) {
                            samples[i] = (float) toMiB(MEMORY.get(Math.max(0, MEMORY.size() - bufferLength) + i));
                        }
                        samples2[i] = (float) i;
                    } catch (Throwable ignored) {}
                }

                if (ImGui.button("Clear")) {
                    MEMORY.clear();
                }
                ImGui.sameLine();

                ImGui.text("Buffer size: %s".formatted(formatSeconds(bufferLength / 10)));
                ImGui.sameLine();
                if (ImGui.button("+")) {
                    if (Screen.hasShiftDown()) {
                        bufferLength += 100;
                    } else {
                        bufferLength += 10;
                    }
                }
                ImGui.sameLine();
                if (ImGui.button("-")) {
                    if (Screen.hasShiftDown()) {
                        bufferLength -= 100;
                    } else {
                        bufferLength -= 10;
                    }
                }

                long maxMemory = Runtime.getRuntime().maxMemory();
                long totalMemory = Runtime.getRuntime().totalMemory();
                long freeMemory = Runtime.getRuntime().freeMemory();
                long usedMemory = totalMemory - freeMemory;
                ImGui.text("Memory: %s%% (%s/%sMiB)".formatted(usedMemory * 100L / maxMemory, toMiB(usedMemory), toMiB(maxMemory)));

                ImPlot.setNextPlotLimits(0, bufferLength, 0, toMiB(maxMemory), ImGuiCond.Always);
                if (ImPlot.beginPlot("Example Plot")) {
                    ImPlot.pushStyleVar(ImPlotStyleVar.FillAlpha, 0.25f);
                    try {
                        ImPlot.plotLine("Line", samples2, samples);
                        ImPlot.plotShaded("Line", samples2, samples, 0, bufferLength);
                    } catch (Throwable ignored) {}
                    ImPlot.endPlot();
                }
            }

            if (ImGui.collapsingHeader("Islands")) {
                if (MESSAGES.containsKey("island.info")) {
                    ImGui.text(MESSAGES.get("island.info").getString());
                }

                for (Island island : IslandLogic.getInstance().islands.stuck) {
                    if (ImGui.button("Scan " + island.hashCode())) { //Each button must have a unique id, and the id is the label ._.
                        if (island.isScanning()) {
                            MESSAGES.put("island.info", Texts.of("Can not queue a scan for an island that is already scanning!"));
                        } else {
                            MESSAGES.put("island.info", Texts.of("Scan queued"));

                            AtomicInteger total = new AtomicInteger();
                            IslandLogic.getInstance().islandScanner.queueScan(island, integer -> {
                                MESSAGES.put("island.info", Texts.of("Scanning %total% chunks", new MapBuilder.StringMap().putAny("total", integer).build()));
                                total.set(integer);
                            }, integer -> {
                                MESSAGES.put("island.info", Texts.of("Scanned %current%/%total% chunks", new MapBuilder.StringMap()
                                  .putAny("total", total.get())
                                  .putAny("current", integer)
                                  .build()));
                            }, (timeTaken, scannedBlocks) -> {
                                MESSAGES.put("island.info", Texts.of("Scanned %total% blocks in %time%", new MapBuilder.StringMap()
                                  .putAny("total", UnitConvertions.readableNumber(scannedBlocks.values().stream().mapToInt(value -> value).sum()))
                                  .putAny("time", UnitConvertions.formatTimings(timeTaken))
                                  .build()));
                            }, () -> {
                                MESSAGES.put("island.info", Texts.of("Island scan failed"));
                            });
                        }
                    }
                    ImGui.sameLine();
                    ImGui.text("%s: %s (%s)".formatted(island.owner.name, island.getIslandId(), island.getPoints()));
                }
            }

            ImGui.end();
        }
    }

    private static long toMiB(long bytes) {
        return bytes / 1024L / 1024L;
    }

    private static String formatSeconds(int seconds) {
        StringBuilder builder = new StringBuilder();
        if (seconds >= 60) {
            int minutes = seconds / 60;
            if (minutes >= 60) {
                int hours = minutes / 60;
                builder.append(hours).append("h");
            }
            if (minutes % 60 > 0) {
                builder.append(minutes % 60).append("m");
            }
        }

        if (seconds % 60 > 0) {
            builder.append(seconds % 60).append("s");
        }

        return builder.toString();
    }
}
