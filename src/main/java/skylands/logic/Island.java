package skylands.logic;

import eu.pb4.common.economy.api.EconomyAccount;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import skylands.SkylandsMain;
import skylands.api.events.IslandEvents;
import skylands.api.island.IslandSettings;
import skylands.api.island.PermissionLevel;
import skylands.api.island.SettingsManager;
import skylands.logic.economy.SkylandsEconomyAccount;
import skylands.util.Constants;
import skylands.util.Players;
import skylands.util.Texts;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.time.Instant;
import java.util.*;

//TODO: Advanced island settings
//TODO: Island levels
public class Island {
    private final MinecraftServer server = skylands.logic.Skylands.getServer();
    private final Fantasy fantasy = skylands.logic.Skylands.getInstance().fantasy;
    private final Map<Identifier, IslandSettings> settings = new HashMap<>();
    public final Member owner;
    @Getter private UUID islandId = UUID.randomUUID();
    private RuntimeWorldConfig islandConfig = null;
    private RuntimeWorldConfig netherConfig = null;
    private RuntimeWorldConfig endConfig = null;
    public ArrayList<Member> members = new ArrayList<>();
    public ArrayList<Member> bans = new ArrayList<>();
    public int radius = SkylandsMain.MAIN_CONFIG.defaultIslandRadius();
    private EconomyAccount wallet;
    boolean freshCreated = false;

    public boolean locked = false;
    public Vec3d spawnPos = SkylandsMain.MAIN_CONFIG.defaultIslandLocation();
    public Vec3d visitsPos = SkylandsMain.MAIN_CONFIG.defaultIslandLocation();
    public boolean hasNether = false;
    public boolean hasEnd = false;

    public Instant created = Instant.now();

    public Island(UUID uuid, String name) {
        this(new Member(uuid, name));
    }

    public Island(PlayerEntity owner) {
        this(new Member(owner));
    }

    public Island(Member owner) {
        this.owner = owner;
        this.wallet = new SkylandsEconomyAccount(islandId, new Identifier(owner.uuid.toString(), islandId.toString()));
        Skylands.getInstance().economy.PROVIDER.getAccounts().computeIfAbsent(islandId, id -> wallet);
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
        island.wallet = new SkylandsEconomyAccount(island.islandId, id, balance);
        Skylands.getInstance().economy.PROVIDER.getAccounts().computeIfAbsent(island.islandId, id1 -> island.wallet);

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

        NbtCompound settingsNbt = nbt.getCompound("settings");
        settingsNbt.getKeys().forEach(key -> {
            NbtCompound settingsDataNbt = settingsNbt.getCompound(key);
            PermissionLevel level = PermissionLevel.fromValue(settingsDataNbt.getString("permission"));
            if (level != null) {
                IslandSettings islandSettings = new IslandSettings(level);
                island.settings.put(new Identifier(key), islandSettings);
            }
        });

        SettingsManager.update(island.settings);

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

        SettingsManager.update(this.settings);
        NbtCompound settingsNbt = new NbtCompound();
        this.settings.forEach((identifier, settings) -> {
            NbtCompound settingsDataNbt = new NbtCompound();
            settingsDataNbt.putString("permission", settings.permissionLevel.getId().toString());
            settingsNbt.put(identifier.toString(), settingsDataNbt);
        });
        nbt.put("settings", settingsNbt);

        return nbt;
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
        if (radius < 0) return true;
        int minY = getOverworld().getBottomY();
        return new Box(new BlockPos(0, 0, 0)).expand(radius).withMinY(minY - 1).withMaxY(getOverworld().getTopY() + 1).contains(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
    }

    public Map<Identifier, IslandSettings> getSettings() {
        return settings;
    }

    public IslandSettings getSettings(Identifier identifier) {
        return settings.computeIfAbsent(identifier, id -> SettingsManager.getDefaultSettings().get(id));
    }

    public boolean isInteractionAllowed(Identifier identifier, PermissionLevel source) {
        return source.getLevel() >= getSettings(identifier).permissionLevel.getLevel();
    }

    public RuntimeWorldHandle getHandler() {
        if (this.islandConfig == null) {
            this.islandConfig = createIslandConfig();
        }
        return this.fantasy.getOrOpenPersistentWorld(SkylandsMain.id(this.owner.uuid.toString()), this.islandConfig);
    }

    private RuntimeWorldConfig createIslandConfig() {
        var biome = this.server.getRegistryManager().get(RegistryKeys.BIOME).getEntry(this.server.getRegistryManager().get(RegistryKeys.BIOME).getOrThrow(BiomeKeys.PLAINS));
        FlatChunkGeneratorConfig flat = new FlatChunkGeneratorConfig(Optional.empty(), biome, List.of());
        FlatChunkGenerator generator = new FlatChunkGenerator(flat);

        return new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.OVERWORLD)
                .setGenerator(generator)
                .setDifficulty(Difficulty.NORMAL)
                .setShouldTickTime(true)
                .setSeed(0L);
    }

    public RuntimeWorldHandle getNetherHandler() {
        if (this.netherConfig == null) {
            this.netherConfig = createNetherConfig();
        }
        return this.fantasy.getOrOpenPersistentWorld(new Identifier(Constants.NAMESPACE_NETHER, this.owner.uuid.toString()), this.netherConfig);
    }

    private RuntimeWorldConfig createNetherConfig() {
        var biome = this.server.getRegistryManager().get(RegistryKeys.BIOME).getEntry(this.server.getRegistryManager().get(RegistryKeys.BIOME).getOrThrow(BiomeKeys.NETHER_WASTES));
        FlatChunkGeneratorConfig flat = new FlatChunkGeneratorConfig(Optional.empty(), biome, List.of());
        FlatChunkGenerator generator = new FlatChunkGenerator(flat);

        return new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.THE_NETHER)
                .setGenerator(generator)
                .setDifficulty(Difficulty.NORMAL)
                .setShouldTickTime(false)
                .setSeed(RandomSeed.getSeed());
    }

    public RuntimeWorldHandle getEndHandler() {
        if (this.endConfig == null) {
            this.endConfig = createEndConfig();
        }
        return this.fantasy.getOrOpenPersistentWorld(new Identifier(Constants.NAMESPACE_END, this.owner.uuid.toString()), this.endConfig);
    }

    private RuntimeWorldConfig createEndConfig() {
        var biome = this.server.getRegistryManager().get(RegistryKeys.BIOME).getEntry(this.server.getRegistryManager().get(RegistryKeys.BIOME).getOrThrow(BiomeKeys.THE_END));
        FlatChunkGeneratorConfig flat = new FlatChunkGeneratorConfig(Optional.empty(), biome, List.of());
        FlatChunkGenerator generator = new FlatChunkGenerator(flat);

        return new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.THE_END)
                .setGenerator(generator)
                .setDifficulty(Difficulty.NORMAL)
                .setShouldTickTime(false)
                .setSeed(RandomSeed.getSeed());
    }

    public ServerWorld getOverworld() {
        RuntimeWorldHandle handler = this.getHandler();
        handler.setTickWhenEmpty(false);
        return handler.asWorld();
    }

    //TODO: End island
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

    public void visit(PlayerEntity player, Vec3d pos) {
        ServerWorld world = this.getOverworld();
        player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), Set.of(), 0, 0);

        if(!isMember(player)) {
            Players.get(this.owner.name).ifPresent(owner -> {
                if(!player.getUuid().equals(owner.getUuid())) {
                    owner.sendMessage(Texts.prefixed("message.skylands.island_visit.visit", map -> map.put("visitor", player.getName().getString())));
                }
            });
        }

        IslandEvents.ON_ISLAND_VISIT.invoker().invoke(player, world, this);

        if (this.freshCreated) {
            this.onFirstLoad(player);
            this.freshCreated = false;
        }
    }

    public void visitAsMember(PlayerEntity player) {
        this.visit(player, this.spawnPos);
    }

    public void visitAsVisitor(PlayerEntity player) {
        this.visit(player, this.visitsPos);
    }

    public void onFirstLoad(PlayerEntity player) {
        ServerWorld world = this.getOverworld();
        StructureTemplate structure = server.getStructureTemplateManager().getTemplateOrBlank(SkylandsMain.id("start_island"));
        StructurePlacementData data = new StructurePlacementData().setMirror(BlockMirror.NONE).setIgnoreEntities(true);
        structure.place(world, new BlockPos(-7, 65, -7), new BlockPos(0, 0, 0), data, world.getRandom(), Block.NOTIFY_ALL);
        IslandEvents.ON_ISLAND_FIRST_LOAD.invoker().invoke(player, world, this);
    }

    void onFirstNetherLoad(ServerWorld world) {
        if (this.hasNether) return;

        MinecraftServer server = world.getServer();

        StructureTemplate structure = server.getStructureTemplateManager().getTemplateOrBlank(SkylandsMain.id("nether_island"));
        StructurePlacementData data = new StructurePlacementData().setMirror(BlockMirror.NONE).setIgnoreEntities(true);
        structure.place(world, new BlockPos(-7, 65, -7), new BlockPos(0, 0, 0), data, world.getRandom(), Block.NOTIFY_ALL);
        IslandEvents.ON_NETHER_FIRST_LOAD.invoker().onLoad(world, this);

        this.hasNether = true;
    }

    void onFirstEndLoad(ServerWorld world) {
        if (this.hasEnd) return;

        MinecraftServer server = world.getServer();

        StructureTemplate structure = server.getStructureTemplateManager().getTemplateOrBlank(SkylandsMain.id("end_island"));
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
            wallet = new SkylandsEconomyAccount(islandId, new Identifier(owner.uuid.toString(), islandId.toString()));
        }
        return wallet;
    }
}
