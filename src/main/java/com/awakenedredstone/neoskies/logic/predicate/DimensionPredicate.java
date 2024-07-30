package com.awakenedredstone.neoskies.logic.predicate;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesRegister;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

import java.util.List;

public record DimensionPredicate(Type type, List<Identifier> dimensions) implements LootCondition {
    public static final MapCodec<DimensionPredicate> CODEC = RecordCodecBuilder.mapCodec(instance ->
      instance.group(
        StringIdentifiable.createCodec(Type::values).optionalFieldOf("type", Type.ALLOW).forGetter(DimensionPredicate::type),
        Identifier.CODEC.listOf().fieldOf("dimensions").forGetter(DimensionPredicate::dimensions)
      ).apply(instance, DimensionPredicate::new)
    );

    @Override
    public LootConditionType getType() {
        return NeoSkiesRegister.Predicates.DIMENSION;
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerWorld world = lootContext.getWorld();

        if (dimensions.contains(world.getRegistryKey().getValue())) {
            return type.result();
        }

        return !type.result();
    }

    enum Type implements StringIdentifiable {
        ALLOW(true),
        DENY(false);

        private final boolean result;

        Type(boolean result) {
            this.result = result;
        }

        public boolean result() {
            return result;
        }

        @Override
        public String asString() {
            return name().toLowerCase();
        }
    }
}
