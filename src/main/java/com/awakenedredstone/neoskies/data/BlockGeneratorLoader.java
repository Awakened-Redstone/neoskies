package com.awakenedredstone.neoskies.data;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesRegister;
import com.awakenedredstone.neoskies.mixin.accessor.TagEntryAccessor;
import com.awakenedredstone.neoskies.util.LinedStringBuilder;
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
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class BlockGeneratorLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    public static final Logger LOGGER = LoggerFactory.getLogger("BlockGenLoader");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final List<BlockGenerator> EMPTY = List.of();

    public static final BlockGeneratorLoader INSTANCE = new BlockGeneratorLoader();

    private final Map<Identifier, List<BlockGenerator>> cache = new HashMap<>();
    private Map<TagEntry, List<BlockGenerator>> generatorMap = ImmutableMap.of();
    private Map<Identifier, BlockGenerator> defaultGeneratorMap = ImmutableMap.of();

    public BlockGeneratorLoader() {
        super(GSON, "block_gen");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        Map<Identifier, BlockGenerator> defaultGeneratorMapBuilder = new HashMap<>();
        Map<TagEntry, List<BlockGenerator>> generatorMapBuilder = new HashMap<>();
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

            defaultGeneratorMapBuilder.put(identifier, blockGenerator);
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
        defaultGeneratorMap = ImmutableMap.copyOf(defaultGeneratorMapBuilder);
        defaultGeneratorMapBuilder.clear();
        generatorMapBuilder.clear();
        cache.clear();
    }

    @Override
    public Identifier getFabricId() {
        return NeoSkies.id("block_gen");
    }

    public Map<Identifier, BlockGenerator> getGenerators() {
        return defaultGeneratorMap;
    }

    public boolean generate(Identifier source, World world, BlockPos pos) {
        cache.computeIfAbsent(source, id -> {
            List<BlockGenerator> generators = new ArrayList<>();

            for (Map.Entry<TagEntry, List<BlockGenerator>> entry : generatorMap.entrySet()) {
                TagEntry tagEntry = entry.getKey();
                List<BlockGenerator> value = entry.getValue();

                TagEntryAccessor accessor = (TagEntryAccessor) tagEntry;

                FluidState defaultState = Registries.FLUID.get(id).getDefaultState();
                TagKey<Fluid> tagKey = TagKey.of(RegistryKeys.FLUID, accessor.getId());
                if ((accessor.isTag() && defaultState.getRegistryEntry().isIn(tagKey)) || accessor.getId().equals(id)) {
                    generators.addAll(value);
                }
            }

            List<BlockGenerator> out = generators.isEmpty() ? EMPTY : List.copyOf(generators);
            generators.clear();
            return out;
        });

        for (BlockGenerator generator : cache.get(source)) {
            BlockPos blockPos = generator.target.test(world, pos);
            if (blockPos != null) {
                return generator.setBlock((ServerWorld) world, pos);
            }
        }

        return false;
    }

    public record BlockGenerator(TagEntry source, Target target, List<GenerationGroup> generates) {
        public static final Codec<BlockGenerator> CODEC = RecordCodecBuilder.create(instance ->
          instance.group(
            TagEntry.CODEC.fieldOf("source").forGetter(BlockGenerator::source),
            Codec.lazyInitialized(() -> Codec.withAlternative(Target.FluidTarget.getCodec(), Target.BlockTarget.getCodec())).fieldOf("target").forGetter(BlockGenerator::target),
            GenerationGroup.CODEC.listOf().fieldOf("generates").forGetter(BlockGenerator::generates)
          ).apply(instance, BlockGenerator::new)
        );

        public BlockState getBlock(ServerWorld world, BlockPos pos) {
            for (GenerationGroup generate : generates) {
                if (generate.predicate.isPresent()) {
                    LootContextParameterSet parameters = new LootContextParameterSet.Builder(world)
                      .add(LootContextParameters.ORIGIN, pos.toCenterPos())
                      .build(NeoSkiesRegister.LootContext.POS);

                    LootContext context = new LootContext.Builder(parameters).build(Optional.empty());
                    if (!generate.predicate.get().test(context)) {
                        continue;
                    }
                }

                Generation randomData = generate.getRandomData(world, pos);
                NeoSkies.LOGGER.info(randomData.nbt.toString());
                return randomData.state();
            }
            return Blocks.LODESTONE.getDefaultState();
        }

        public boolean setBlock(ServerWorld world, BlockPos pos) {
            for (GenerationGroup generate : generates) {
                if (generate.predicate.isPresent()) {
                    LootContextParameterSet parameters = new LootContextParameterSet.Builder(world)
                      .add(LootContextParameters.ORIGIN, pos.toCenterPos())
                      .build(NeoSkiesRegister.LootContext.POS);

                    LootContext context = new LootContext.Builder(parameters).build(Optional.empty());
                    if (!generate.predicate.get().test(context)) {
                        continue;
                    }
                }

                Generation randomData = generate.getRandomData(world, pos);

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

        public static abstract class Target {
            public abstract @Nullable BlockPos test(World world, BlockPos pos);
            public abstract @NotNull String id();
            public abstract @NotNull Item icon();
            public abstract @NotNull String description();

            public static class FluidTarget extends Target {
                public static final Codec<FluidTarget> CODEC = TagEntry.CODEC.comapFlatMap(id -> DataResult.success(new FluidTarget(id)), FluidTarget::getFluid);

                private final TagEntry fluid;

                public FluidTarget(TagEntry fluid) {
                    this.fluid = fluid;
                }

                public static Codec<Target> getCodec() {
                    return (Codec<Target>) (Codec<? extends Target>) CODEC;
                }

                @Override
                public @NotNull String id() {
                    return "fluid";
                }

                @Override
                public @NotNull Item icon() {
                    return Items.COBBLESTONE;
                }

                @Override
                public @NotNull String description() {
                    return getFluid().toString();
                }

                public TagEntry getFluid() {
                    return fluid;
                }

                @Override
                public BlockPos test(World world, BlockPos pos) {
                    for (Direction direction : FluidBlock.FLOW_DIRECTIONS) {
                        BlockPos blockPos = pos.offset(direction.getOpposite());
                        FluidState fluidState = world.getFluidState(blockPos);
                        if (fluidState.isEmpty()) {
                            continue;
                        }

                        TagEntryAccessor accessor = (TagEntryAccessor) fluid;

                        TagKey<Fluid> tagKey = TagKey.of(RegistryKeys.FLUID, accessor.getId());
                        if ((accessor.isTag() && fluidState.getRegistryEntry().isIn(tagKey)) || accessor.getId().equals(Registries.FLUID.getId(fluidState.getFluid()))) {
                            return blockPos;
                        }
                    }

                    return null;
                }
            }

            public static class BlockTarget extends Target {
                public static final Codec<BlockTarget> CODEC = RecordCodecBuilder.create(instance ->
                  instance.group(
                    Identifier.CODEC.fieldOf("surface").forGetter(BlockTarget::getSurface),
                    Identifier.CODEC.fieldOf("touching").forGetter(BlockTarget::getTouching)
                  ).apply(instance, BlockTarget::new)
                );

                public final Identifier surface;
                public final Identifier touching;

                public BlockTarget(@NotNull Identifier surface, @NotNull Identifier touching) {
                    this.surface = surface;
                    this.touching = touching;
                }

                public static Codec<Target> getCodec() {
                    return (Codec<Target>) (Codec<? extends Target>) CODEC;
                }

                @Override
                public @NotNull String id() {
                    return "block";
                }

                @Override
                public @NotNull Item icon() {
                    return Items.BASALT;
                }

                @Override
                public @NotNull String description() {
                    return new LinedStringBuilder()
                      .appendLine()
                      .append("|  Surface: ").append(surface)
                      .appendLine("|  Touching: ").append(touching)
                      .toString();
                }

                public Identifier getSurface() {
                    return surface;
                }

                public Identifier getTouching() {
                    return touching;
                }

                @Override
                public BlockPos test(World world, BlockPos pos) {
                    boolean bl = world.getBlockState(pos.down()).isOf(Registries.BLOCK.get(surface));

                    for (Direction direction : FluidBlock.FLOW_DIRECTIONS) {
                        BlockPos blockPos = pos.offset(direction.getOpposite());

                        if (!bl || !world.getBlockState(blockPos).isOf(Registries.BLOCK.get(touching))) continue;
                        return blockPos;
                    }

                    return null;
                }
            }
        }

        public static final class GenerationGroup {
            public static final Codec<GenerationGroup> CODEC = RecordCodecBuilder.create(instance ->
              instance.group(
                Generation.CODEC.listOf().fieldOf("blocks").forGetter(GenerationGroup::blocks),
                LootConditionTypes.CODEC.optionalFieldOf("predicate").forGetter(GenerationGroup::predicate)
              ).apply(instance, GenerationGroup::new)
            );
            private final List<Generation> blocks;
            private final Optional<LootCondition> predicate;
            private final WeightedRandom<Generation> weightedRandom;

            public GenerationGroup(List<Generation> blocks, Optional<LootCondition> predicate) {
                this.blocks = blocks;
                this.predicate = predicate;
                this.weightedRandom = new WeightedRandom<>();
            }

            public List<Generation> blocks() {
                return blocks;
            }

            public Optional<LootCondition> predicate() {
                return predicate;
            }

            public WeightedRandom<Generation> weightedRandom() {
                return weightedRandom;
            }

            public Generation getRandomData(ServerWorld world, BlockPos pos) {
                for (Generation block : blocks) {
                    if (block.predicate.isPresent()) {
                        LootContextParameterSet parameters = new LootContextParameterSet.Builder(world)
                          .add(LootContextParameters.ORIGIN, pos.toCenterPos())
                          .build(NeoSkiesRegister.LootContext.POS);

                        LootContext context = new LootContext.Builder(parameters).build(Optional.empty());
                        if (!block.predicate.get().test(context)) {
                            continue;
                        }
                    }

                    weightedRandom.add(block.weight, block);
                }

                Generation generation = weightedRandom.next();
                weightedRandom.clear();
                return generation;
            }

            public GenerationGroup mutable() {
                return new GenerationGroup(new ArrayList<>(blocks), predicate);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (GenerationGroup) obj;
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

        public record Generation(BlockState state, Optional<NbtCompound> nbt, int weight, Optional<LootCondition> predicate) {
            public static final MapCodec<BlockState> BLOCK_STATE_CODEC = Registries.BLOCK.getCodec().dispatchMap("id", state -> state.owner, owner -> {
                BlockState state = owner.getDefaultState();
                if (state.getEntries().isEmpty()) {
                    return MapCodec.unit(state);
                }
                return state.codec.codec().optionalFieldOf("properties").xmap(optional -> optional.orElse(state), Optional::of);
            }).stable();

            public static final Codec<Generation> CODEC = RecordCodecBuilder.create(instance ->
              instance.group(
                RecordCodecBuilder.of(Generation::state, BLOCK_STATE_CODEC),
                NbtCompound.CODEC.optionalFieldOf("nbt").forGetter(Generation::nbt),
                Codec.INT.fieldOf("weight").forGetter(Generation::weight),
                LootConditionTypes.CODEC.optionalFieldOf("predicate").forGetter(Generation::predicate)
              ).apply(instance, Generation::new)
            );
        }
    }
}
