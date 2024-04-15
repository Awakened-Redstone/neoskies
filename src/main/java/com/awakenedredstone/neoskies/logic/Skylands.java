package com.awakenedredstone.neoskies.logic;

import com.awakenedredstone.neoskies.config.IslandRanking;
import com.awakenedredstone.neoskies.config.MainConfig;
import eu.pb4.common.protection.api.CommonProtection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import com.awakenedredstone.neoskies.SkylandsMain;
import com.awakenedredstone.neoskies.logic.economy.Economy;
import com.awakenedredstone.neoskies.logic.protection.SkylandsProtectionProvider;
import com.awakenedredstone.neoskies.util.NbtMigrator;
import com.awakenedredstone.neoskies.util.PreInitData;
import com.awakenedredstone.neoskies.util.Scheduler;
import xyz.nucleoid.fantasy.Fantasy;

public class Skylands {
    private int format = 1;
    private static Skylands instance;
    private final MinecraftServer server;
    public final Fantasy fantasy;
    public final IslandStuck islands;
    public final Hub hub;
    public final Invites invites;
    public final Economy economy;
    public final Scheduler scheduler;
    private final SkylandsProtectionProvider protectionProvider;
    private static final MainConfig CONFIG = new MainConfig();
    private static final IslandRanking RANKING_CONFIG = new IslandRanking();

    public Skylands(MinecraftServer server) {
        this.scheduler = new Scheduler();
        this.server = server;
        this.fantasy = Fantasy.get(server);
        this.islands = new IslandStuck();
        this.hub = new Hub();
        this.invites = new Invites();
        this.economy = new Economy();
        this. protectionProvider = new SkylandsProtectionProvider();
        CommonProtection.register(SkylandsMain.id("neoskies"), protectionProvider);
    }

    public static Skylands getInstance() {
        return instance;
    }

    public int getFormat() {
        return format;
    }

    public void readFromNbt(NbtCompound nbt) {
        NbtCompound skylandsNbt = nbt.getCompound("neoskies");
        if (skylandsNbt.isEmpty()) return;

        NbtMigrator.update(skylandsNbt);

        this.format = skylandsNbt.getInt("format");
        this.hub.readFromNbt(skylandsNbt);
        this.islands.readFromNbt(skylandsNbt);
    }

    public void writeToNbt(NbtCompound nbt) {
        NbtCompound skylandsNbt = new NbtCompound();

        skylandsNbt.putInt("format", this.format);
        this.islands.writeToNbt(skylandsNbt);
        this.hub.writeToNbt(skylandsNbt);

        nbt.put("neoskies", skylandsNbt);
    }

    //Lock the instance so noone can possibly change it
    public static void init(MinecraftServer server) {
        if (Skylands.instance != null) throw new IllegalStateException("Skylands already has been initialized!");
        Skylands.instance = new Skylands(server);
    }

    public void onTick(MinecraftServer server) {
        this.invites.tick(server);
        this.scheduler.tick(server);
    }

    public void close() {
        Skylands.instance = null;
        this.scheduler.close();
        CommonProtection.remove(SkylandsMain.id("neoskies"));
    }

    public static MinecraftServer getServer() {
        return getInstance().server;
    }

    public static Scheduler getScheduler() {
        return getInstance().scheduler;
    }

    public static ResourceManager getResourceManager() {
        return instance == null ? PreInitData.getInstance().getResourceManager() : Skylands.getServer().getResourceManager();
    }

    public static MainConfig getConfig() {
        return CONFIG;
    }

    public static IslandRanking getRankingConfig() {
        return RANKING_CONFIG;
    }

    public static void syncWithTick(Runnable runnable) {
        Skylands.getScheduler().schedule(0, runnable);
    }
}
