package com.awakenedredstone.neoskies.logic;

import com.awakenedredstone.neoskies.api.events.IslandEvents;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.Worlds;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Hub {
    public Vec3d pos = new Vec3d(0, 80, 0);
    public boolean hasProtection = false;
    //public PositionSongPlayer songPlayer = null;

    public void visit(ServerPlayerEntity player) {
        visit(player, false);
    }

    public void visit(ServerPlayerEntity player, boolean silent) {
        var world = IslandLogic.getServer().getOverworld();
        if (!silent) player.sendMessage(Texts.prefixed("message.neoskies.hub_visit"));
        Worlds.teleport(player, world, this.pos, 0, 0);
        IslandEvents.ON_HUB_VISIT.invoker().invoke(player, world);
    }

    public void positionInto(ServerPlayerEntity player) {
        player.setPosition(this.pos);
        player.setVelocity(0, 0, 0);
        player.setYaw(0);
        player.setPitch(0);
    }

    /*public void initSongPlayer(MinecraftServer server) {
        PositionSongPlayer sp = new PositionSongPlayer(SongsData.INSTANCE.playlist, server.getOverworld());
        sp.setDistance(256);
        sp.setId(NeoSkies.id("hub_song_player"));
        sp.setBlockPos(BlockPos.ofFloored(pos));
        this.songPlayer = sp;
    }*/

    public void readFromNbt(NbtCompound nbt) {
        NbtCompound hubNbt = nbt.getCompound("hub");
        this.pos = new Vec3d(hubNbt.getDouble("x"), hubNbt.getDouble("y"), hubNbt.getDouble("z"));
        this.hasProtection = hubNbt.getBoolean("hasProtection");
    }

    public void writeToNbt(NbtCompound nbt) {
        NbtCompound hubNbt = new NbtCompound();
        hubNbt.putDouble("x", this.pos.x);
        hubNbt.putDouble("y", this.pos.y);
        hubNbt.putDouble("z", this.pos.z);
        hubNbt.putBoolean("hasProtection", this.hasProtection);
        nbt.put("hub", hubNbt);
    }
}
