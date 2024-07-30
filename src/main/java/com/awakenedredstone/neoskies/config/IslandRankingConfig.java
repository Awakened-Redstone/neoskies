package com.awakenedredstone.neoskies.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.JsonPrimitive;
import com.awakenedredstone.neoskies.config.source.Config;
import com.awakenedredstone.neoskies.config.source.JanksonBuilder;
import com.awakenedredstone.neoskies.config.source.annotation.PredicateConstraint;
import com.awakenedredstone.neoskies.config.source.annotation.RangeConstraint;
import com.awakenedredstone.neoskies.config.source.annotation.SkipThis;
import com.awakenedredstone.neoskies.mixin.accessor.TagEntryAccessor;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class IslandRankingConfig extends Config {
    public IslandRankingConfig() {
        super("neoskies/ranking", JanksonBuilder.buildJankson(builder -> {
            builder.registerSerializer(TagEntry.class, (tagEntry, marshaller) -> {
                DataResult<JsonElement> result = TagEntry.CODEC.encodeStart(JsonOps.INSTANCE, tagEntry);
                return JsonPrimitive.of(result.getOrThrow().getAsString());
            });
            builder.registerDeserializer(JsonPrimitive.class, TagEntry.class, (jsonPrimitive, m) -> {
                com.google.gson.JsonPrimitive json = new com.google.gson.JsonPrimitive(jsonPrimitive.asString());
                return TagEntry.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
            });
        }));
    }

    @RangeConstraint(min = 0, max = Integer.MAX_VALUE)
    @Comment("The default amount of points a block that isn't in the list gets")
    public int defaultValue = 1;

    @Comment("The formula used by for the level calculation, use \"points\" (without quotes) to use the island points on the math")
    public String formula = "points / 100";

    @PredicateConstraint("pointConstraint")
    @Comment("The value of each block for the island ranking, tags are allowed")
    public Map<TagEntry, Integer> points = new LinkedHashMap<>();

    @SkipThis
    private Map<Identifier, Integer> cache = new HashMap<>();

    public int getPoints(Identifier identifier) {
        return cache.computeIfAbsent(identifier, id -> {
            for (Map.Entry<TagEntry, Integer> entry : points.entrySet()) {
                TagEntry tagEntry = entry.getKey();
                Integer value = entry.getValue();

                TagEntryAccessor accessor = (TagEntryAccessor) tagEntry;
                if (accessor.isTag() && Registries.BLOCK.get(id).getDefaultState().getRegistryEntry().isIn(TagKey.of(RegistryKeys.BLOCK, accessor.getId()))) {
                    return value;
                } else if (accessor.getId().equals(id)) {
                    return value;
                }
            }

            return defaultValue;
        });
    }

    @Override
    public void load() {
        cache = new LinkedHashMap<>();
        super.load();
    }

    public static boolean pointConstraint(Map<Identifier, Integer> points) {
        for (int value : points.values()) {
            if (value < 0) return false;
        }
        return true;
    }
}
