package com.awakenedredstone.neoskies.logic;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.api.events.IslandEvents;
import com.awakenedredstone.neoskies.api.island.CurrentSettings;
import com.awakenedredstone.neoskies.api.island.PermissionLevel;
import com.awakenedredstone.neoskies.logic.economy.NeoSkiesEconomyAccount;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesRegistries;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import com.awakenedredstone.neoskies.logic.settings.IslandSettingsUtil;
import com.awakenedredstone.neoskies.util.Constants;
import com.awakenedredstone.neoskies.util.Players;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.Worlds;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.ParseException;
import com.google.common.collect.ImmutableList;
import eu.pb4.common.economy.api.EconomyAccount;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

//TODO: Island levels
//TODO: Advanced island settings
public class Island {
    protected final Fantasy fantasy = IslandLogic.getInstance().fantasy;
    protected final Map<Identifier, CurrentSettings> settings = new LinkedHashMap<>();
    public final Member owner;
    private UUID islandId = UUID.randomUUID();
    protected RuntimeWorldConfig islandConfig = null;
    protected RuntimeWorldConfig netherConfig = null;
    protected RuntimeWorldConfig endConfig = null;
    public final List<Member> members = new ArrayList<>();
    public final List<Member> bans = new ArrayList<>();
    public int radius = IslandLogic.getConfig().defaultIslandRadius;
    private EconomyAccount wallet;
    boolean freshCreated = false;

    public boolean locked = false;
    public Vec3d spawnPos = IslandLogic.getConfig().defaultIslandLocation;
    public Vec3d visitsPos = IslandLogic.getConfig().defaultIslandLocation;
    protected Map<Identifier, Integer> blocks = new LinkedHashMap<>();
    private long points = 0;
    private long level = 0;
    public boolean hasNether = false;
    public boolean hasEnd = false;

    private boolean scanning = false;

    private Instant created = Instant.now();

    public Island(UUID uuid, String name) {
        this(new Member(uuid, name));
    }

    public Island(PlayerEntity owner) {
        this(new Member(owner));
    }

    public Island(Member owner) {
        this.owner = owner;
        this.wallet = new NeoSkiesEconomyAccount(islandId, new Identifier(owner.uuid.toString(), islandId.toString()));
        IslandLogic.getInstance().ECONOMY.PROVIDER.getAccounts().computeIfAbsent(islandId, id -> wallet);
    }

    public static Island fromNbt(NbtCompound nbt) {
        Island island = new Island(Member.fromNbt(nbt.getCompound("owner")));
        island.islandId = nbt.getUuid("id");
        island.hasNether = nbt.getBoolean("hasNether");
        island.hasEnd = nbt.getBoolean("hasEnd");
        island.created = Instant.parse(nbt.getString("created"));
        island.locked = nbt.getBoolean("locked");
        island.radius = nbt.getInt("radius");
        island.freshCreated = nbt.getBoolean("freshCreated");

        NbtCompound walletNbt = nbt.getCompound("wallet");
        Identifier id = new Identifier(walletNbt.getString("id"));
        long balance = walletNbt.getLong("balance");
        island.wallet = new NeoSkiesEconomyAccount(island.islandId, id, balance);
        IslandLogic.getInstance().ECONOMY.PROVIDER.getAccounts().computeIfAbsent(island.islandId, id1 -> island.wallet);

        NbtCompound spawnPosNbt = nbt.getCompound("spawnPos");
        double spawnPosX = spawnPosNbt.getDouble("x");
        double spawnPosY = spawnPosNbt.getDouble("y");
        double spawnPosZ = spawnPosNbt.getDouble("z");
        island.spawnPos = new Vec3d(spawnPosX, spawnPosY, spawnPosZ);

        NbtCompound visitsPosNbt = nbt.getCompound("visitsPos");
        double visitsPosX = visitsPosNbt.getDouble("x");
        double visitsPosY = visitsPosNbt.getDouble("y");
        double visitsPosZ = visitsPosNbt.getDouble("z");
        island.visitsPos = new Vec3d(visitsPosX, visitsPosY, visitsPosZ);

        NbtCompound membersNbt = nbt.getCompound("members");
        int membersSize = membersNbt.getInt("size");
        for (int i = 0; i < membersSize; i++) {
            NbtCompound member = membersNbt.getCompound(String.valueOf(i));
            island.members.add(Member.fromNbt(member));
        }

        NbtCompound bansNbt = nbt.getCompound("bans");
        int bansSize = bansNbt.getInt("size");
        for (int i = 0; i < bansSize; i++) {
            NbtCompound member = bansNbt.getCompound(String.valueOf(i));
            island.bans.add(Member.fromNbt(member));
        }

        for (IslandSettings islandSetting : NeoSkiesRegistries.ISLAND_SETTINGS) {
            CurrentSettings currentSettings = new CurrentSettings(islandSetting, islandSetting.getDefaultLevel());
            island.settings.put(islandSetting.getIdentifier(), currentSettings);
        }

        NbtCompound settingsNbt = nbt.getCompound("settings");
        settingsNbt.getKeys().forEach(key -> {
            Identifier identifier = new Identifier(key);
            NbtCompound settingsDataNbt = settingsNbt.getCompound(key);
            PermissionLevel level = PermissionLevel.fromValue(settingsDataNbt.getString("permission"));
            IslandSettings islandSettings = NeoSkiesRegistries.ISLAND_SETTINGS.get(identifier);
            if (level != null) {
                CurrentSettings currentSettings = new CurrentSettings(islandSettings, level);
                island.settings.put(identifier, currentSettings);
            }
        });

        island.points = nbt.getLong("points");
        island.level = nbt.getLong("level");
        NbtCompound blocksNbt = nbt.getCompound("blocks");
        blocksNbt.getKeys().forEach(key -> {
            int amount = blocksNbt.getInt(key);
            island.blocks.put(new Identifier(key), amount);
        });

        //Sort island block count
        List<Map.Entry<Identifier, Integer>> entries = new LinkedList<>(island.blocks.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getValue));
        Collections.reverse(entries);
        island.blocks.clear();
        entries.forEach(entry -> island.blocks.put(entry.getKey(), entry.getValue()));

        return island;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("owner", this.owner.toNbt());
        nbt.putUuid("id", this.islandId);
        nbt.putBoolean("hasNether", this.hasNether);
        nbt.putBoolean("hasEnd", this.hasEnd);
        nbt.putString("created", this.created.toString());
        nbt.putBoolean("locked", this.locked);
        nbt.putInt("radius", radius);
        nbt.putBoolean("freshCreated", this.freshCreated);

        NbtCompound walletNbt = new NbtCompound();
        walletNbt.putString("id", wallet.id().toString());
        walletNbt.putLong("balance", wallet.balance());
        nbt.put("wallet", walletNbt);

        NbtCompound spawnPosNbt = new NbtCompound();
        spawnPosNbt.putDouble("x", this.spawnPos.getX());
        spawnPosNbt.putDouble("y", this.spawnPos.getY());
        spawnPosNbt.putDouble("z", this.spawnPos.getZ());
        nbt.put("spawnPos", spawnPosNbt);

        NbtCompound visitsPosNbt = new NbtCompound();
        visitsPosNbt.putDouble("x", this.visitsPos.getX());
        visitsPosNbt.putDouble("y", this.visitsPos.getY());
        visitsPosNbt.putDouble("z", this.visitsPos.getZ());
        nbt.put("visitsPos", visitsPosNbt);

        NbtCompound membersNbt = new NbtCompound();
        membersNbt.putInt("size", this.members.size());
        for (int i = 0; i < this.members.size(); i++) {
            Member member = this.members.get(i);
            NbtCompound memberNbt = member.toNbt();
            membersNbt.put(Integer.toString(i), memberNbt);
        }
        nbt.put("members", membersNbt);

        NbtCompound bansNbt = new NbtCompound();
        bansNbt.putInt("size", this.bans.size());
        for (int i = 0; i < this.bans.size(); i++) {
            Member bannedMember = this.bans.get(i);
            NbtCompound bannedNbt = bannedMember.toNbt();
            bansNbt.put(Integer.toString(i), bannedNbt);
        }
        nbt.put("bans", bansNbt);

        for (IslandSettings islandSetting : NeoSkiesRegistries.ISLAND_SETTINGS) {
            CurrentSettings currentSettings = new CurrentSettings(islandSetting, islandSetting.getDefaultLevel());
            this.settings.put(islandSetting.getIdentifier(), currentSettings);
        }

        NbtCompound settingsNbt = new NbtCompound();
        this.settings.forEach((identifier, settings) -> {
            NbtCompound settingsDataNbt = new NbtCompound();
            settingsDataNbt.putString("permission", settings.getPermissionLevel().getId().toString());
            settingsNbt.put(identifier.toString(), settingsDataNbt);
        });
        nbt.put("settings", settingsNbt);

        nbt.putLong("points", this.points);
        nbt.putLong("level", this.level);
        NbtCompound blocksNbt = new NbtCompound();
        this.blocks.forEach((block, amount) -> blocksNbt.putInt(block.toString(), amount));
        nbt.put("blocks", blocksNbt);

        return nbt;
    }

    public UUID getIslandId() {
        return islandId;
    }

    public Map<Identifier, Integer> getBlocks() {
        return blocks;
    }

    void setPoints(long points) {
        this.points = points;
    }

    public long getPoints() {
        return points;
    }

    void setLevel(long level) {
        this.level = level;
    }

    public long getLevel() {
        return level;
    }

    public boolean isScanning() {
        return scanning;
    }

    public Instant getCreated() {
        return created;
    }

    public void setScanning(boolean scanning) {
        this.scanning = scanning;
    }

    public List<Member> getAllMembers() {
        return ImmutableList.<Member>builder().addAll(members).add(owner).build();
    }

    public boolean isMember(PlayerEntity player) {
        if (this.owner.uuid.equals(player.getUuid())) {
            return true;
        }
        for (var member : this.members) {
            if (member.uuid.equals(player.getUuid())) return true;
        }
        return false;
    }

    public boolean isMember(String name) {
        if (this.owner.name.equals(name)) {
            return true;
        }
        for (var member : this.members) {
            if (member.name.equals(name)) return true;
        }
        return false;
    }

    public boolean isBanned(PlayerEntity player) {
        for (var bannedMember : this.bans) {
            if (bannedMember.uuid.equals(player.getUuid())) return true;
        }
        return false;
    }

    public boolean isBanned(String player) {
        for (var bannedMember : this.bans) {
            if (bannedMember.name.equals(player)) return true;
        }
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isWithinBorder(BlockPos pos) {
        if (radius <= 0) return true;
        int minY = getOverworld().getBottomY();
        return new Box(new BlockPos(0, 0, 0)).expand(radius).withMinY(minY - 1).withMaxY(getOverworld().getTopY() + 1).contains(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
    }

    public Map<Identifier, CurrentSettings> getSettings() {
        return settings;
    }

    @Nullable
    public CurrentSettings getSettings(Identifier identifier) {
        return settings.computeIfAbsent(identifier, IslandSettingsUtil::getModifiable);
    }

    public boolean isInteractionAllowed(Identifier identifier, PermissionLevel source) {
        CurrentSettings settings = getSettings(identifier);
        if (settings == null) {
            throw new NullPointerException("No Island Settings exist for the provided identifier " + identifier.toString());
        }
        return source.getLevel() >= settings.getPermissionLevel().getLevel();
    }

    private RuntimeWorldConfig createOverworldConfig() {
        var biome = IslandLogic.getServer().getRegistryManager().get(RegistryKeys.BIOME).getEntry(IslandLogic.getServer().getRegistryManager().get(RegistryKeys.BIOME).getOrThrow(BiomeKeys.PLAINS));
        FlatChunkGeneratorConfig flat = new FlatChunkGeneratorConfig(Optional.empty(), biome, List.of());
        FlatChunkGenerator generator = new FlatChunkGenerator(flat);

        return new RuntimeWorldConfig()
          .setDimensionType(DimensionTypes.OVERWORLD)
          .setGenerator(generator)
          .setMirrorOverworldDifficulty(true)
          .setMirrorOverworldGameRules(true)
          .setShouldTickTime(true)
          .setSeed(0L);
    }

    private RuntimeWorldConfig createNetherConfig() {
        var biome = IslandLogic.getServer().getRegistryManager().get(RegistryKeys.BIOME).getEntry(IslandLogic.getServer().getRegistryManager().get(RegistryKeys.BIOME).getOrThrow(BiomeKeys.NETHER_WASTES));
        FlatChunkGeneratorConfig flat = new FlatChunkGeneratorConfig(Optional.empty(), biome, List.of());
        FlatChunkGenerator generator = new FlatChunkGenerator(flat);

        return new RuntimeWorldConfig()
          .setDimensionType(DimensionTypes.THE_NETHER)
          .setGenerator(generator)
          .setDifficulty(Difficulty.NORMAL)
          .setShouldTickTime(false)
          .setSeed(RandomSeed.getSeed());
    }

    private RuntimeWorldConfig createEndConfig() {
        var biome = IslandLogic.getServer().getRegistryManager().get(RegistryKeys.BIOME).getEntry(IslandLogic.getServer().getRegistryManager().get(RegistryKeys.BIOME).getOrThrow(BiomeKeys.THE_END));
        FlatChunkGeneratorConfig flat = new FlatChunkGeneratorConfig(Optional.empty(), biome, List.of());
        FlatChunkGenerator generator = new FlatChunkGenerator(flat);

        return new RuntimeWorldConfig()
          .setDimensionType(DimensionTypes.THE_END)
          .setGenerator(generator)
          .setDifficulty(Difficulty.NORMAL)
          .setShouldTickTime(false)
          .setSeed(RandomSeed.getSeed());
    }

    public RuntimeWorldHandle getOverworldHandler() {
        if (this.islandConfig == null) {
            this.islandConfig = createOverworldConfig();
        }
        return this.fantasy.getOrOpenPersistentWorld(NeoSkies.id(this.owner.uuid.toString()), this.islandConfig);
    }

    public RuntimeWorldHandle getNetherHandler() {
        if (this.netherConfig == null) {
            this.netherConfig = createNetherConfig();
        }
        return this.fantasy.getOrOpenPersistentWorld(new Identifier(Constants.NAMESPACE_NETHER, this.owner.uuid.toString()), this.netherConfig);
    }

    public RuntimeWorldHandle getEndHandler() {
        if (this.endConfig == null) {
            this.endConfig = createEndConfig();
        }
        return this.fantasy.getOrOpenPersistentWorld(new Identifier(Constants.NAMESPACE_END, this.owner.uuid.toString()), this.endConfig);
    }

    public ServerWorld getOverworld() {
        RuntimeWorldHandle handler = this.getOverworldHandler();
        handler.setTickWhenEmpty(false);
        return handler.asWorld();
    }

    public ServerWorld getEnd() {
        RuntimeWorldHandle handler = this.getEndHandler();
        handler.setTickWhenEmpty(false);
        ServerWorld world = handler.asWorld();
        if (!this.hasEnd) this.onFirstEndLoad(world);
        return world;
    }

    public ServerWorld getNether() {
        RuntimeWorldHandle handler = this.getNetherHandler();
        handler.setTickWhenEmpty(false);
        ServerWorld world = handler.asWorld();
        if (!this.hasNether) this.onFirstNetherLoad(world);
        return world;
    }

    public RegistryKey<World> getOverworldKey() {
        return getOverworld().getRegistryKey();
    }

    public RegistryKey<World> getNetherKey() {
        return getNether().getRegistryKey();
    }

    public RegistryKey<World> getEndKey() {
        return getEnd().getRegistryKey();
    }

    public void updateBlocks(@NotNull Map<Identifier, Integer> blocks) throws EvaluationException, ParseException {
        Map<Identifier, Integer> blocksCopy = this.blocks;
        long pointsCopy = this.points;
        long levelCopy = this.level;

        try {
            this.blocks = blocks;
            this.points = 0;
            this.blocks.forEach((block, integer) -> {
                int points = IslandLogic.getRankingConfig().getPoints(block);
                this.points += (long) integer * points;
            });

            Expression expression = new Expression(IslandLogic.getRankingConfig().formula, Constants.EXPRESSION_PARSER).and("points", points);
            EvaluationValue evaluationValue = expression.evaluate();

            if (!evaluationValue.isNumberValue()) {
                throw new IllegalArgumentException("Level formula must be a numeric formula!");
            }

            this.level = evaluationValue.getNumberValue().intValue();
        } catch (Throwable e) {
            this.blocks = blocksCopy;
            this.points = pointsCopy;
            this.level = levelCopy;

            NeoSkies.LOGGER.error("Failed to update island point data, reverting to last state", e);
            throw e;
        }
    }

    public void visit(ServerPlayerEntity player, Vec3d pos) {
        ServerWorld world = this.getOverworld();
        Worlds.teleport(player, world, pos.getX(), pos.getY(), pos.getZ(), 0, 0);

        if (!isMember(player)) {
            Players.get(this.owner.name).ifPresent(owner -> {
                if (!player.getUuid().equals(owner.getUuid())) {
                    owner.sendMessage(Texts.prefixed("message.neoskies.island_visit.visit", map -> map.put("visitor", player.getName().getString())));
                }
            });
        }

        IslandEvents.ON_ISLAND_VISIT.invoker().invoke(player, world, this);

        if (this.freshCreated) {
            this.onFirstLoad(player);
            this.freshCreated = false;
        }
    }

    public void visitAsMember(ServerPlayerEntity player) {
        this.visit(player, this.spawnPos);
    }

    public void visitAsVisitor(ServerPlayerEntity player) {
        this.visit(player, this.visitsPos);
    }

    public void onFirstLoad(PlayerEntity player) {
        ServerWorld world = this.getOverworld();
        StructureTemplate structure = IslandLogic.getServer().getStructureTemplateManager().getTemplateOrBlank(NeoSkies.id("start_island"));
        StructurePlacementData data = new StructurePlacementData().setMirror(BlockMirror.NONE).setIgnoreEntities(true);
        structure.place(world, new BlockPos(-7, 65, -7), new BlockPos(0, 0, 0), data, world.getRandom(), Block.NOTIFY_ALL);
        IslandEvents.ON_ISLAND_FIRST_LOAD.invoker().invoke(player, world, this);
    }

    void onFirstNetherLoad(ServerWorld world) {
        if (this.hasNether) return;

        MinecraftServer server = world.getServer();

        StructureTemplate structure = server.getStructureTemplateManager().getTemplateOrBlank(NeoSkies.id("nether_island"));
        StructurePlacementData data = new StructurePlacementData().setMirror(BlockMirror.NONE).setIgnoreEntities(true);
        structure.place(world, new BlockPos(-7, 65, -7), new BlockPos(0, 0, 0), data, world.getRandom(), Block.NOTIFY_ALL);
        IslandEvents.ON_NETHER_FIRST_LOAD.invoker().onLoad(world, this);

        this.hasNether = true;
    }

    void onFirstEndLoad(ServerWorld world) {
        if (this.hasEnd) return;

        MinecraftServer server = world.getServer();

        StructureTemplate structure = server.getStructureTemplateManager().getTemplateOrBlank(NeoSkies.id("end_island"));
        StructurePlacementData data = new StructurePlacementData().setMirror(BlockMirror.NONE).setIgnoreEntities(true);
        structure.place(world, new BlockPos(-7, 65, -7), new BlockPos(0, 0, 0), data, world.getRandom(), Block.NOTIFY_ALL);
        IslandEvents.ON_END_FIRST_LOAD.invoker().onLoad(world, this);

        this.hasEnd = true;
    }

    public Identifier getIslandIdentifier() {
        return new Identifier(owner.uuid.toString(), islandId.toString());
    }

    public EconomyAccount getWallet() {
        if (wallet == null) {
            wallet = new NeoSkiesEconomyAccount(islandId, new Identifier(owner.uuid.toString(), islandId.toString()));
        }
        return wallet;
    }
}
