package com.awakenedredstone.neoskies.config;

import blue.endless.jankson.Comment;
import com.awakenedredstone.neoskies.config.source.Config;
import com.awakenedredstone.neoskies.config.source.JanksonBuilder;
import com.awakenedredstone.neoskies.config.source.annotation.PredicateConstraint;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class IslandRanking extends Config {
    public IslandRanking() {
        super("neoskies/ranking", JanksonBuilder.buildJankson());
    }

    @PredicateConstraint("pointConstraint")
    @Comment("The value of each block for the island ranking")
    public Map<Identifier, Integer> points = new LinkedHashMap<>();

    public static boolean pointConstraint(Map<Identifier, Integer> points) {
        for (Integer value : points.values()) {
            if (value < 0) return false;
        }
        return true;
    }
}
