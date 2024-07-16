package com.awakenedredstone.neoskies.data;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.WeightedRandom;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockTypes;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.State;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BlockGeneratorLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    public static final Logger LOGGER = LoggerFactory.getLogger("BlockGenLoader");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final List<BlockGenerator> EMPTY = List.of();

    public static final BlockGeneratorLoader INSTANCE = new BlockGeneratorLoader();

    private Map<Identifier, List<BlockGenerator>> generatorMap = ImmutableMap.of();

    public BlockGeneratorLoader() {
        super(GSON, "block_gen");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        Map<Identifier, List<BlockGenerator>> generatorMapBuilder = new HashMap<>();
        prepared.forEach((identifier, jsonElement) -> {
            DataResult<BlockGenerator> dataResult = BlockGenerator.CODEC.parse(JsonOps.INSTANCE, jsonElement);
            Optional<BlockGenerator> generator = dataResult.result();
            if (generator.isEmpty()) {
                LOGGER.error("Failed to parse {}", identifier);
                try {
                    dataResult.getOrThrow();
                } catch (Throwable e) {
                    LOGGER.error("Failed to get data from codecs ", e);
                }
                return;
            }
            BlockGenerator blockGenerator = generator.get();
            List<BlockGenerator> blockGenerators = generatorMapBuilder.get(blockGenerator.source);
            if (blockGenerators == null) {
                ArrayList<BlockGenerator> list = new ArrayList<>();
                list.add(blockGenerator);
                generatorMapBuilder.put(blockGenerator.source, list);
            } else {
                blockGenerators.add(blockGenerator);
            }
        });
        generatorMap = ImmutableMap.copyOf(generatorMapBuilder);
        generatorMapBuilder.clear();
    }

    @Override
    public Identifier getFabricId() {
        return NeoSkies.id("block_gen");
    }

    public BlockGenerator getGenerator(Identifier source, Identifier target) {
        return generatorMap.getOrDefault(source, EMPTY).stream().filter(gen -> gen.target.equals(target)).findFirst().orElse(null);
    }

    public record BlockGenerator(Identifier source, Identifier target, List<GenSet> generates) {
        public static final Codec<BlockGenerator> CODEC = RecordCodecBuilder.create(instance ->
          instance.group(
            Identifier.CODEC.fieldOf("source").forGetter(BlockGenerator::source),
            Identifier.CODEC.fieldOf("target").forGetter(BlockGenerator::target),
            GenSet.CODEC.listOf().fieldOf("generates").forGetter(BlockGenerator::generates)
          ).apply(instance, BlockGenerator::new)
        );

        public BlockState getBlock(ServerWorld world) {
            for (GenSet generate : generates) {
                if (generate.predicate.isPresent()) {
                    LootContextParameterSet.Builder parameters = new LootContextParameterSet.Builder(world);

                    LootContext.Builder context = new LootContext.Builder(parameters.build(LootContextTypes.EMPTY));
                    if (!generate.predicate.get().test(context.build(Optional.empty()))) {
                        continue;
                    }
                }

                GenData randomData = generate.getRandomData(world);
                NeoSkies.LOGGER.info(randomData.nbt.toString());
                return randomData.state();
            }
            return Blocks.LODESTONE.getDefaultState();
        }

        public boolean setBlock(ServerWorld world, BlockPos pos) {
            for (GenSet generate : generates) {
                if (generate.predicate.isPresent()) {
                    LootContextParameterSet.Builder parameters = new LootContextParameterSet.Builder(world);

                    LootContext.Builder context = new LootContext.Builder(parameters.build(LootContextTypes.EMPTY));
                    if (!generate.predicate.get().test(context.build(Optional.empty()))) {
                        continue;
                    }
                }

                GenData randomData = generate.getRandomData(world);

                BlockEntity blockEntity;
                BlockState blockState = Block.postProcessState(randomData.state(), world, pos);
                if (blockState.isAir()) {
                    blockState = randomData.state();
                }
                if (!world.setBlockState(pos, blockState)) {
                    return false;
                }
                if (randomData.nbt().isPresent() && (blockEntity = world.getBlockEntity(pos)) != null) {
                    blockEntity.read(randomData.nbt().get(), world.getRegistryManager());
                }
                return true;
            }
            return false;
        }

        public static final class GenSet {
            public static final Codec<GenSet> CODEC = RecordCodecBuilder.create(instance ->
              instance.group(
                GenData.CODEC.listOf().fieldOf("blocks").forGetter(GenSet::blocks),
                LootConditionTypes.CODEC.optionalFieldOf("predicate").forGetter(GenSet::predicate)
              ).apply(instance, GenSet::new)
            );
            private final List<GenData> blocks;
            private final Optional<LootCondition> predicate;
            private final WeightedRandom<GenData> weightedRandom;

            public GenSet(List<GenData> blocks, Optional<LootCondition> predicate) {
                this.blocks = blocks;
                this.predicate = predicate;
                this.weightedRandom = new WeightedRandom<>();
            }

            public List<GenData> blocks() {
                return blocks;
            }

            public Optional<LootCondition> predicate() {
                return predicate;
            }

            public WeightedRandom<GenData> weightedRandom() {
                return weightedRandom;
            }

            public GenData getRandomData(ServerWorld world) {
                for (GenData block : blocks) {
                    if (block.predicate.isPresent()) {
                        LootContextParameterSet.Builder parameters = new LootContextParameterSet.Builder(world);

                        LootContext.Builder context = new LootContext.Builder(parameters.build(LootContextTypes.EMPTY));
                        if (!block.predicate.get().test(context.build(Optional.empty()))) {
                            continue;
                        }
                    }

                    weightedRandom.add(block.weight, block);
                }

                GenData genData = weightedRandom.next();
                weightedRandom.clear();
                return genData;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (GenSet) obj;
                return Objects.equals(this.blocks, that.blocks) &&
                  Objects.equals(this.weightedRandom, that.weightedRandom);
            }

            @Override
            public int hashCode() {
                return Objects.hash(blocks, weightedRandom);
            }

            @Override
            public String toString() {
                return "GenSet[" +
                  "blocks=" + blocks + ", " +
                  "weightedRandom=" + weightedRandom +
                  ']';
            }
        }

        public record GenData(BlockState state, Optional<NbtCompound> nbt, int weight, Optional<LootCondition> predicate) {
            public static final MapCodec<BlockState> BLOCK_STATE_CODEC = Registries.BLOCK.getCodec().dispatchMap("id", state -> state.owner, owner -> {
                BlockState state = owner.getDefaultState();
                if (state.getEntries().isEmpty()) {
                    return MapCodec.unit(state);
                }
                return state.codec.codec().optionalFieldOf("properties").xmap(optional -> optional.orElse(state), Optional::of);
            }).stable();

            public static final Codec<GenData> CODEC = RecordCodecBuilder.create(instance ->
              instance.group(
                RecordCodecBuilder.of(GenData::state, BLOCK_STATE_CODEC),
                NbtCompound.CODEC.optionalFieldOf("nbt").forGetter(GenData::nbt),
                Codec.INT.fieldOf("weight").forGetter(GenData::weight),
                LootConditionTypes.CODEC.optionalFieldOf("predicate").forGetter(GenData::predicate)
              ).apply(instance, GenData::new)
            );
        }
    }
}
