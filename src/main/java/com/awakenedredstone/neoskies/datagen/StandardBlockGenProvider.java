package com.awakenedredstone.neoskies.datagen;

import com.awakenedredstone.neoskies.data.BlockGeneratorLoader;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.fluid.Fluid;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class StandardBlockGenProvider implements DataProvider {
    private final DataOutput.PathResolver pathResolver;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;
    private final String modId;
    private Map<String, GeneratorBuilder> generators = new HashMap<>();

    public StandardBlockGenProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        this.pathResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, "block_gen");
        this.registryLookupFuture = registriesFuture;
        this.modId = output.getModId();
    }

    public abstract void generate();

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return this.registryLookupFuture.thenCompose(registryLookup -> this.run(writer, (RegistryWrapper.WrapperLookup)registryLookup));
    }

    private CompletableFuture<?> run(DataWriter writer, RegistryWrapper.WrapperLookup registryLookup) {
        generate();

        return CompletableFuture.allOf(generators.entrySet().stream().map(entry -> {
            String key = entry.getKey();
            GeneratorBuilder generator = entry.getValue();

            Path path = this.pathResolver.resolveJson(Identifier.of(modId, key));
            return DataProvider.writeCodecToPath(writer, registryLookup, BlockGeneratorLoader.BlockGenerator.CODEC, generator.build(), path);
        }).toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Block Generation Tables";
    }

    public GeneratorBuilder createGenerator(String id, TagKey<?> source, BlockGeneratorLoader.BlockGenerator.Target target) {
        GeneratorBuilder builder = new GeneratorBuilder(TagEntry.createTag(source.id()), target);
        generators.put(id, builder);
        return builder;
    }

    public GeneratorBuilder createGenerator(String id, Fluid source, BlockGeneratorLoader.BlockGenerator.Target target) {
        GeneratorBuilder builder = new GeneratorBuilder(source, target);
        generators.put(id, builder);
        return builder;
    }

    public GeneratorBuilder createGenerator(String id, TagKey<?> source, TagKey<?> target) {
        GeneratorBuilder builder = new GeneratorBuilder(TagEntry.createTag(source.id()), TagEntry.createTag(target.id()));
        generators.put(id, builder);
        return builder;
    }

    public GeneratorBuilder createGenerator(String id, Fluid source, TagKey<?> target) {
        GeneratorBuilder builder = new GeneratorBuilder(source, TagEntry.createTag(target.id()));
        generators.put(id, builder);
        return builder;
    }

    public GeneratorBuilder createGenerator(String id, Fluid source, Fluid target) {
        GeneratorBuilder builder = new GeneratorBuilder(source, target);
        generators.put(id, builder);
        return builder;
    }

    public GeneratorBuilder createGenerator(String id, TagKey<?> source, Fluid target) {
        GeneratorBuilder builder = new GeneratorBuilder(TagEntry.createTag(source.id()), target);
        generators.put(id, builder);
        return builder;
    }

    public SetBuilder generatorSet() {
        return new SetBuilder();
    }

    public static class GeneratorBuilder {
        private final TagEntry source;
        private final BlockGeneratorLoader.BlockGenerator.Target target;
        private final List<BlockGeneratorLoader.BlockGenerator.GenerationGroup> generates = new ArrayList<>();

        public GeneratorBuilder(TagEntry source, BlockGeneratorLoader.BlockGenerator.Target target) {
            this.source = source;
            this.target = target;
        }

        public GeneratorBuilder(Fluid source, BlockGeneratorLoader.BlockGenerator.Target target) {
            this(TagEntry.create(Registries.FLUID.getId(source)), target);
        }

        public GeneratorBuilder(TagEntry source, TagEntry target) {
            this(source, new BlockGeneratorLoader.BlockGenerator.Target.FluidTarget(target));
        }

        public GeneratorBuilder(Fluid source, TagEntry target) {
            this(TagEntry.create(Registries.FLUID.getId(source)), target);
        }

        public GeneratorBuilder(Fluid source, Fluid target) {
            this(TagEntry.create(Registries.FLUID.getId(source)), TagEntry.create(Registries.FLUID.getId(target)));
        }

        public GeneratorBuilder(TagEntry source, Fluid target) {
            this(source, TagEntry.create(Registries.FLUID.getId(target)));
        }

        public static GeneratorBuilder from(BlockGeneratorLoader.BlockGenerator generator) {
            GeneratorBuilder builder = new GeneratorBuilder(generator.source(), generator.target());
            builder.generates.addAll(generator.generates());
            return builder;
        }

        public GeneratorBuilder addOutput(SetBuilder output) {
            generates.add(output.build());
            return this;
        }

        public BlockGeneratorLoader.BlockGenerator build() {
            /*if (generates.isEmpty()) {
                throw new IllegalArgumentException("Tried to build an empty block generator!");
            }*/

            return new BlockGeneratorLoader.BlockGenerator(source, target, generates);
        }
    }

    public static class SetBuilder {
        private final List<BlockGeneratorLoader.BlockGenerator.Generation> blocks = new ArrayList<>();
        private LootCondition predicate;

        public SetBuilder setPredicate(LootCondition predicate) {
            this.predicate = predicate;
            return this;
        }

        public SetBuilder addBlock(Block block, int weight) {
            blocks.add(new BlockGeneratorLoader.BlockGenerator.Generation(block.getDefaultState(), Optional.empty(), weight, Optional.empty()));
            return this;
        }

        public SetBuilder addBlock(BlockState blockState, int weight) {
            blocks.add(new BlockGeneratorLoader.BlockGenerator.Generation(blockState, Optional.empty(), weight, Optional.empty()));
            return this;
        }

        public SetBuilder addBlock(@NotNull Block block, int weight, @Nullable LootCondition predicate) {
            blocks.add(new BlockGeneratorLoader.BlockGenerator.Generation(block.getDefaultState(), Optional.empty(), weight, Optional.ofNullable(predicate)));
            return this;
        }

        public SetBuilder addBlock(@NotNull BlockState blockState, int weight, @Nullable LootCondition predicate) {
            blocks.add(new BlockGeneratorLoader.BlockGenerator.Generation(blockState, Optional.empty(), weight, Optional.ofNullable(predicate)));
            return this;
        }

        public SetBuilder addBlock(@NotNull BlockState blockState, @NotNull NbtCompound nbt, int weight, @Nullable LootCondition predicate) {
            blocks.add(new BlockGeneratorLoader.BlockGenerator.Generation(blockState, Optional.of(nbt), weight, Optional.ofNullable(predicate)));
            return this;
        }

        private BlockGeneratorLoader.BlockGenerator.GenerationGroup build() {
            if (blocks.isEmpty()) {
                throw new IllegalArgumentException("Tried to build an empty generator set!");
            }

            return new BlockGeneratorLoader.BlockGenerator.GenerationGroup(blocks, Optional.ofNullable(predicate));
        }
    }
}
