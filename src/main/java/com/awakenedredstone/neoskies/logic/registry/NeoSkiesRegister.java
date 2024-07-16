package com.awakenedredstone.neoskies.logic.registry;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.logic.predicate.IslandLevelPredicate;
import com.awakenedredstone.neoskies.logic.protection.NeoSkiesProtectionProvider;
import com.mojang.serialization.MapCodec;
import eu.pb4.common.protection.api.CommonProtection;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class NeoSkiesRegister {
    public static void init() {
        CommonProtection.register(NeoSkies.id("neoskies"), new NeoSkiesProtectionProvider());
        Predicates.init();
    }

    public static class Predicates {
        public static final LootConditionType ISLAND_LEVEL = registerPredicate("island_level", IslandLevelPredicate.CODEC);

        private static LootConditionType registerPredicate(String id, MapCodec<? extends LootCondition> codec) {
            return Registry.register(Registries.LOOT_CONDITION_TYPE, NeoSkies.id(id), new LootConditionType(codec));
        }

        public static void init() {}
    }
}
