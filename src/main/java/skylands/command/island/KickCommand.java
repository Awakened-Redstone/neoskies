package skylands.command.island;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import skylands.api.SkylandsAPI;
import skylands.logic.Skylands;
import skylands.util.Texts;

import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static skylands.command.utils.CommandUtils.*;

public class KickCommand {

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("kick")
                .requires(requiresIsland("skylands.command.kick", true))
                .then(argument("player", player())
                    .executes(context -> {
                        var player = context.getSource().getPlayer();
                        var kickedPlayer = EntityArgumentType.getPlayer(context, "player");
                        if (player != null && kickedPlayer != null) {
                            KickCommand.run(player, kickedPlayer);
                        }
                        return 1;
                    })
                )
            )
        );
    }

    static void run(ServerPlayerEntity player, ServerPlayerEntity kicked) {
        Skylands.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            if (player.getName().getString().equals(kicked.getName().getString())) {
                player.sendMessage(Texts.prefixed("message.skylands.kick_visitor.yourself"));
            } else {
                if (island.isMember(kicked)) {
                    player.sendMessage(Texts.prefixed("message.skylands.kick_visitor.member"));
                } else {
                    SkylandsAPI.getIsland(kicked.getWorld()).ifPresent(isl -> {
                        if (isl.owner.uuid.equals(island.owner.uuid)) {
                            player.sendMessage(Texts.prefixed("message.skylands.kick_visitor.success", map -> map.put("player", kicked.getName().getString())));

                            kicked.sendMessage(Texts.prefixed("message.skylands.kick_visitor.kick", map -> map.put("owner", player.getName().getString())));
                            Skylands.getInstance().hub.visit(player);
                        } else {
                            player.sendMessage(Texts.prefixed("message.skylands.kick_visitor.fail", map -> map.put("player", kicked.getName().getString())));
                        }
                    });
                }
            }
        }, () -> player.sendMessage(Texts.prefixed("message.skylands.kick_visitor.no_island")));
    }
}
