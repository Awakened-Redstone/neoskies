package com.awakenedredstone.neoskies.test;

import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Constants;
import com.awakenedredstone.neoskies.util.MapBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.UnitConvertions;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DebugImGuiRenderer implements ImGuiRenderer {
    private static final ImPlotContext IMPLOT_CONTEXT;
    public static final ImBoolean OPEN = new ImBoolean(false);
    private static final Map<String, Text> MESSAGES = new HashMap<>();
    private static Island selectedIsland = null;
    private static final ImBoolean SELECTED_ISLAND_OPEN = new ImBoolean(false);
    private static Float[] islandLevelCurve = null;
    private static float islandLevelCurveMax = 0;

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
        if (OPEN.get()) {
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
                        } catch (Throwable ignored) { }
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
                    if (ImPlot.beginPlot("Server RAM usage")) {
                        ImPlot.pushStyleVar(ImPlotStyleVar.FillAlpha, 0.25f);
                        try {
                            ImPlot.plotLine("Line", samples2, samples);
                            ImPlot.plotShaded("Line", samples2, samples, 0, bufferLength);
                        } catch (Throwable ignored) { }
                        ImPlot.endPlot();
                    }
                }

                if (ImGui.collapsingHeader("Islands")) {
                    for (Island island : IslandLogic.getInstance().islands.stuck) {
                        ImGui.spacing();
                        if (ImGui.button("Open panel##" + island.getIslandId())) { //Each button must have a unique id, and the id is the label ._.
                            if (island.equals(selectedIsland)) {
                                selectedIsland = null;
                            } else {
                                selectedIsland = island;
                                SELECTED_ISLAND_OPEN.set(true);
                            }
                        }
                        ImGui.indent();
                        ImGui.text("Owner: " + island.owner.name);
                        ImGui.text("ID: " + island.getIslandId());
                        ImGui.spacing();
                        ImGui.unindent();
                    }

                    if (ImGui.collapsingHeader("Island level")) {
                        if (ImGui.button("Clear data")) {
                            islandLevelCurve = null;
                            islandLevelCurveMax = Integer.MIN_VALUE;
                        }

                        int pointPool = 1_000_000;
                        if (islandLevelCurve == null) {
                            islandLevelCurve = new Float[100];
                            Arrays.fill(islandLevelCurve, 0f);
                            for (int i = 0, j = 0; j < islandLevelCurve.length; i += pointPool / islandLevelCurve.length, j++) {
                                try {
                                    Expression expression = new Expression(IslandLogic.getRankingConfig().formula, Constants.EXPRESSION_PARSER).and("points", i);
                                    EvaluationValue evaluationValue = expression.evaluate();

                                    if (!evaluationValue.isNumberValue()) {
                                        continue;
                                    }

                                    float level = evaluationValue.getNumberValue().floatValue();
                                    islandLevelCurve[j] = level;
                                    if (level > islandLevelCurveMax) {
                                        islandLevelCurveMax = level;
                                    }
                                } catch (Throwable ignored) { }
                            }
                        }

                        final Float[] samples = new Float[islandLevelCurve.length];
                        Arrays.fill(samples, 0f);
                        for (int i = 0, j = 0; j < islandLevelCurve.length; i += pointPool / islandLevelCurve.length, j++) {
                            samples[j] = (float) i;
                        }

                        ImPlot.setNextPlotLimits(0, pointPool, 0, islandLevelCurveMax, ImGuiCond.Always);
                        if (ImPlot.beginPlot("Level curve")) {
                            ImPlot.pushStyleVar(ImPlotStyleVar.FillAlpha, 0.25f);
                            try {
                                ImPlot.plotLine("Points", samples, islandLevelCurve);
                            } catch (Throwable ignored) { }
                            ImPlot.endPlot();
                        }
                    }
                }
            }
            ImGui.end();
        }

        if (!SELECTED_ISLAND_OPEN.get() && selectedIsland != null) {
            selectedIsland = null;
        }

        if (selectedIsland != null) {
            renderIslandWindow();
        }
    }

    private static void renderIslandWindow() {
        if (ImGui.begin(selectedIsland.owner.name + "'s island", SELECTED_ISLAND_OPEN, ImGuiWindowFlags.MenuBar)) {
            ImGui.text("Radius: " + selectedIsland.radius);
            ImGui.text("Points: " + selectedIsland.getPoints());
            ImGui.text("Level: " + selectedIsland.getLevel());

            if (ImGui.collapsingHeader("Island scan")) {
                if (!MESSAGES.containsKey("island.scan.status")) {
                    MESSAGES.put("island.scan.status", Texts.literal("Not scanning"));
                }
                ImGui.text(MESSAGES.get("island.scan.status").getString());
                if (ImGui.button("Scan")) {
                    if (selectedIsland.isScanning()) {
                        MESSAGES.put("island.scan.status", Texts.translatable("commands.neoskies.level.scan.error.busy"));
                    } else {
                        MESSAGES.put("island.scan.status", Texts.translatable("commands.neoskies.level.scan.queued"));

                        AtomicInteger total = new AtomicInteger();
                        IslandLogic.getInstance().islandScanner.queueScan(selectedIsland, integer -> {
                            MESSAGES.put("island.scan.status", Texts.translatable("commands.neoskies.level.scan.total", new MapBuilder.StringMap().putAny("total", integer).build()));
                            total.set(integer);
                        }, integer -> {
                            MESSAGES.put("island.scan.status", Texts.translatable("commands.neoskies.level.scan.progress", new MapBuilder.StringMap()
                              .putAny("total", total.get())
                              .putAny("current", integer)
                              .build()));
                        }, (timeTaken, scannedBlocks) -> {
                            MESSAGES.put("island.scan.status", Texts.translatable("commands.neoskies.level.scan.finished", new MapBuilder.StringMap()
                              .putAny("total", UnitConvertions.readableNumber(scannedBlocks.values().stream().mapToInt(value -> value).sum()))
                              .putAny("time", UnitConvertions.formatTimings(timeTaken))
                              .build()));
                        }, () -> {
                            MESSAGES.put("island.scan.status", Texts.translatable("commands.neoskies.level.scan.error"));
                        });
                    }
                }
            }
        }
        ImGui.end();
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
