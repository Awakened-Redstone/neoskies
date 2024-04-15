package com.awakenedredstone.neoskies.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
//import nota.player.SongPlayer;
import com.awakenedredstone.neoskies.api.SkylandsAPI;
import com.awakenedredstone.neoskies.logic.Member;
import com.awakenedredstone.neoskies.logic.Skylands;
import com.awakenedredstone.neoskies.util.Texts;

@SuppressWarnings("unused")
public class PlayerConnectEvent {

    public static void onJoin(MinecraftServer server, ServerPlayerEntity player) {
        Skylands.getInstance().islands.getByPlayer(player).ifPresent(island -> island.owner.name = player.getName().getString());

        Skylands.getInstance().islands.stuck.forEach(island -> {
            for (Member member : island.members) {
                if (member.uuid.equals(player.getUuid())) {
                    member.name = player.getName().getString();
                }
            }
            for (Member bannedMember : island.bans) {
                if (bannedMember.uuid.equals(player.getUuid())) {
                    bannedMember.name = player.getName().getString();
                }
            }
        });

        SkylandsAPI.getIsland(player.getWorld()).ifPresent(island -> {
            if (!island.isMember(player) && island.isBanned(player)) {
                player.sendMessage(Texts.prefixed("message.neoskies.ban_player.ban", map -> map.put("owner", island.owner.name)));
                Skylands.getInstance().hub.visit(player);
            }
        });
    }

    public static void onLeave(MinecraftServer server, ServerPlayerEntity player) {

    }
}
