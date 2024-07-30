package com.awakenedredstone.neoskies.logic;

import com.awakenedredstone.neoskies.config.IslandRankingConfig;
import com.awakenedredstone.neoskies.config.MainConfig;
import com.awakenedredstone.neoskies.logic.economy.Economy;
import com.awakenedredstone.neoskies.logic.level.IslandScanner;
import com.awakenedredstone.neoskies.util.NbtMigrator;
import com.awakenedredstone.neoskies.util.PreInitData;
import com.awakenedredstone.neoskies.util.Scheduler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.fantasy.Fantasy;

public class IslandLogic {
    public static final Economy ECONOMY = new Economy();
    private static final MainConfig CONFIG = new MainConfig();
    private static final IslandRankingConfig RANKING_CONFIG = new IslandRankingConfig();

    private int format = 1;
    private static IslandLogic instance;
    private final MinecraftServer server;
    public final Fantasy fantasy;
    public final IslandStuck islands = new IslandStuck();
    public final Hub hub = new Hub();
    public final Invites invites = new Invites();
    public final Scheduler scheduler = new Scheduler();
    public final IslandScanner islandScanner = new IslandScanner();

    public IslandLogic(MinecraftServer server) {
        this.server = server;
        this.fantasy = Fantasy.get(server);
    }

    public static IslandLogic getInstance() {
        return instance;
    }

    public int getFormat() {
        return format;
    }

    public void readFromNbt(NbtCompound nbt) {
        NbtCompound neoskiesNbt = nbt.getCompound("neoskies");
        if (neoskiesNbt.isEmpty()) return;

        NbtMigrator.update(neoskiesNbt);

        this.format = neoskiesNbt.getInt("format");
        this.hub.readFromNbt(neoskiesNbt);
        this.islands.readFromNbt(neoskiesNbt);
    }

    public void writeToNbt(NbtCompound nbt) {
        NbtCompound neoskiesNbt = new NbtCompound();

        neoskiesNbt.putInt("format", this.format);
        this.islands.writeToNbt(neoskiesNbt);
        this.hub.writeToNbt(neoskiesNbt);

        nbt.put("neoskies", neoskiesNbt);
    }

    //Lock the instance so noone can possibly change it
    public static void init(MinecraftServer server) {
        if (IslandLogic.instance != null) throw new IllegalStateException("NeoSkies already has been initialized!");
        IslandLogic.instance = new IslandLogic(server);
    }

    public void onTick(MinecraftServer server) {
        this.invites.tick(server);
        this.scheduler.tick(server);
    }

    public void close() {
        IslandLogic.instance = null;
        this.scheduler.close();
    }

    public static MinecraftServer getServer() {
        return getInstance().server;
    }

    public static Scheduler getScheduler() {
        return getInstance().scheduler;
    }

    public static ResourceManager getResourceManager() {
        return instance == null ? PreInitData.getInstance().getResourceManager() : IslandLogic.getServer().getResourceManager();
    }

    public static MainConfig getConfig() {
        return CONFIG;
    }

    public static IslandRankingConfig getRankingConfig() {
        return RANKING_CONFIG;
    }

    public static void runOnNextTick(Runnable runnable) {
        IslandLogic.getScheduler().schedule(0, runnable);
    }
    public static void scheduleDelayed(long delay, Runnable runnable) {
        IslandLogic.getScheduler().scheduleDelayed(getServer(), delay, runnable);
    }
}
