package com.awakenedredstone.neoskies.logic.predicate;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesRegister;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.StringIdentifiable;

public record IslandLevelPredicate(int amount, Type type, boolean islandOnly) implements LootCondition {
    public static final MapCodec<IslandLevelPredicate> CODEC = RecordCodecBuilder.mapCodec(instance ->
      instance.group(
        Codec.INT.fieldOf("amount").forGetter(IslandLevelPredicate::amount),
        StringIdentifiable.createCodec(Type::values).optionalFieldOf("type", Type.LEVEL).forGetter(IslandLevelPredicate::type),
        Codec.BOOL.optionalFieldOf("islandOnly", true).forGetter(IslandLevelPredicate::islandOnly)
      ).apply(instance, IslandLevelPredicate::new)
    );

    @Override
    public LootConditionType getType() {
        return NeoSkiesRegister.Predicates.ISLAND_LEVEL;
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerWorld world = lootContext.getWorld();
        if (NeoSkiesAPI.isIsland(world)) {
            Island island = NeoSkiesAPI.getIsland(world);
            long islandAmount = switch (type) {
                case POINTS -> island.getPoints();
                case LEVEL -> island.getLevel();
            };

            return islandAmount >= this.amount;
        }
        return false;
    }

    public enum Type implements StringIdentifiable {
        LEVEL,
        POINTS;

        @Override
        public String asString() {
            return name().toLowerCase();
        }
    }
}
