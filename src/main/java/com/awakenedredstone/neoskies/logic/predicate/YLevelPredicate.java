package com.awakenedredstone.neoskies.logic.predicate;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesRegister;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public record YLevelPredicate(int minLevel, int maxLevel, boolean minInclusive, boolean maxInclusive) implements LootCondition {
    public static final MapCodec<YLevelPredicate> CODEC = RecordCodecBuilder.mapCodec(instance ->
      instance.group(
        Codec.INT.optionalFieldOf("minLevel", Integer.MIN_VALUE).forGetter(YLevelPredicate::minLevel),
        Codec.INT.optionalFieldOf("maxLevel", Integer.MAX_VALUE).forGetter(YLevelPredicate::maxLevel),
        Codec.BOOL.optionalFieldOf("minInclusive", true).forGetter(YLevelPredicate::minInclusive),
        Codec.BOOL.optionalFieldOf("maxInclusive", true).forGetter(YLevelPredicate::maxInclusive)
      ).apply(instance, YLevelPredicate::new)
    );

    @Override
    public LootConditionType getType() {
        return NeoSkiesRegister.Predicates.Y_LEVEL;
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerWorld world = lootContext.getWorld();
        Vec3d originPos = lootContext.get(LootContextParameters.ORIGIN);
        if (originPos == null) {
            return false;
        }

        BlockPos origin = BlockPos.ofFloored(originPos);
        int y = origin.getY();

        return (minInclusive ? y >= minLevel : y > minLevel) && (maxInclusive ? y <= maxLevel : y < maxLevel);
    }
}
