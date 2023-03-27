package skylands.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import nota.player.SongPlayer;
import skylands.api.SkylandsAPI;
import skylands.logic.Member;
import skylands.logic.Skylands;
import skylands.util.Texts;

@SuppressWarnings("unused")
public class PlayerConnectEvent {

    public static void onJoin(MinecraftServer server, ServerPlayerEntity player) {
        SongPlayer sp = Skylands.instance.hub.songPlayer;
        if (sp != null) {
            sp.addPlayer(player);
        }

        Skylands.instance.islands.get(player).ifPresent(island -> island.owner.name = player.getName().getString());

        Skylands.instance.islands.stuck.forEach(island -> {
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
                player.sendMessage(Texts.prefixed("message.skylands.ban_player.ban", map -> map.put("%owner%", island.owner.name)));
                Skylands.instance.hub.visit(player);
            }
        });
    }

    public static void onLeave(MinecraftServer server, ServerPlayerEntity player) {

    }
}
