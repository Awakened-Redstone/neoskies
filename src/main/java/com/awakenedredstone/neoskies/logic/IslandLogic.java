package com.awakenedredstone.neoskies.logic;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.config.IslandRankingConfig;
import com.awakenedredstone.neoskies.config.MainConfig;
import com.awakenedredstone.neoskies.logic.economy.Economy;
import com.awakenedredstone.neoskies.logic.protection.NeoSkiesProtectionProvider;
import com.awakenedredstone.neoskies.util.NbtMigrator;
import com.awakenedredstone.neoskies.util.PreInitData;
import com.awakenedredstone.neoskies.util.Scheduler;
import eu.pb4.common.protection.api.CommonProtection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.fantasy.Fantasy;

public class IslandLogic {
    private int format = 1;
    private static IslandLogic instance;
    private final MinecraftServer server;
    public final Fantasy fantasy;
    public final IslandStuck islands;
    public final Hub hub;
    public final Invites invites;
    public final Economy economy;
    public final Scheduler scheduler;
    private final NeoSkiesProtectionProvider protectionProvider;
    private static final MainConfig CONFIG = new MainConfig();
    private static final IslandRankingConfig RANKING_CONFIG = new IslandRankingConfig();

    public IslandLogic(MinecraftServer server) {
        this.scheduler = new Scheduler();
        this.server = server;
        this.fantasy = Fantasy.get(server);
        this.islands = new IslandStuck();
        this.hub = new Hub();
        this.invites = new Invites();
        this.economy = new Economy();
        this. protectionProvider = new NeoSkiesProtectionProvider();
        CommonProtection.register(NeoSkies.id("neoskies"), protectionProvider);
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
        CommonProtection.remove(NeoSkies.id("neoskies"));
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

    public static void syncWithTick(Runnable runnable) {
        IslandLogic.getScheduler().schedule(0, runnable);
    }
}
