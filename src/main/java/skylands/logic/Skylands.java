package skylands.logic;

import lombok.Getter;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import skylands.logic.economy.Economy;
import skylands.util.NbtMigrator;
import xyz.nucleoid.fantasy.Fantasy;

public class Skylands {
    public int format = 4;
    @Getter
    private static Skylands instance;
    private final MinecraftServer server;
    public Fantasy fantasy;
    public IslandStuck islands;
    public Hub hub;
    public Invites invites;
    public Economy economy;

    public Skylands(MinecraftServer server) {
        this.server = server;
        this.fantasy = Fantasy.get(server);
        this.islands = new IslandStuck();
        this.hub = new Hub();
        this.invites = new Invites();
        this.economy = new Economy();
    }

    public void readFromNbt(NbtCompound nbt) {
        NbtCompound skylandsNbt = nbt.getCompound("skylands");
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

        nbt.put("skylands", skylandsNbt);
    }

    //Lock the instance so noone can possibly change it
    public static void init(MinecraftServer server) {
        if (Skylands.instance != null) throw new IllegalStateException("Skylands already has been initialized!");
        Skylands.instance = new Skylands(server);
    }

    public void onTick(MinecraftServer server) {
        this.invites.tick(server);
    }

    public static MinecraftServer getServer() {
        return getInstance().server;
    }

}
