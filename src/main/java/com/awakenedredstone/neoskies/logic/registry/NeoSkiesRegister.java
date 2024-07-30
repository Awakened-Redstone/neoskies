package com.awakenedredstone.neoskies.logic.registry;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.logic.predicate.DimensionPredicate;
import com.awakenedredstone.neoskies.logic.predicate.IslandLevelPredicate;
import com.awakenedredstone.neoskies.logic.predicate.YLevelPredicate;
import com.awakenedredstone.neoskies.logic.protection.NeoSkiesProtectionProvider;
import com.mojang.serialization.MapCodec;
import eu.pb4.common.protection.api.CommonProtection;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class NeoSkiesRegister {
    public static void init() {
        CommonProtection.register(NeoSkies.id("neoskies"), new NeoSkiesProtectionProvider());
        Predicates.init();
        LootContext.init();
    }

    public static class LootContext {
        public static final LootContextType POS = LootContextTypes.register("pos", builder -> builder.require(LootContextParameters.ORIGIN));

        public static void init() { }
    }

    public static class Predicates {
        public static final LootConditionType ISLAND_LEVEL = registerPredicate("island_level", IslandLevelPredicate.CODEC);
        public static final LootConditionType Y_LEVEL = registerPredicate("y_level", YLevelPredicate.CODEC);
        public static final LootConditionType DIMENSION = registerPredicate("dimension", DimensionPredicate.CODEC);

        private static LootConditionType registerPredicate(String id, MapCodec<? extends LootCondition> codec) {
            return Registry.register(Registries.LOOT_CONDITION_TYPE, NeoSkies.id(id), new LootConditionType(codec));
        }

        public static void init() { }
    }
}
