package skylands.command.island;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import skylands.data.SkylandComponents;
import skylands.logic.Skylands;
import skylands.util.Players;
import skylands.util.Texts;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static skylands.command.utils.CommandUtils.node;
import static skylands.command.utils.CommandUtils.register;

public class AcceptCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("accept")
                .requires(Permissions.require("skylands.command.accept", true))
                .then(argument("player", word())
                    .executes(context -> {
                        String inviter = StringArgumentType.getString(context, "player");
                        var player = context.getSource().getPlayer();

                        if (player != null) {
                            AcceptCommand.run(player, inviter);
                        }
                        return 1;
                    })
                )
            )
        );
    }

    static void run(ServerPlayerEntity player, String ownerName) {
        var inviter = Players.get(ownerName);
        if (inviter.isPresent()) {
            var island = Skylands.getInstance().islands.getByPlayer(inviter.get());
            if (island.isPresent()) {
                var invite = Skylands.getInstance().invites.get(island.get(), player);
                if (invite.isPresent()) {
                    if (!invite.get().accepted) {
                        invite.get().accept(player);
                        player.sendMessage(Texts.prefixed("message.skylands.accept.success", map -> map.put("owner", ownerName)));
                        SkylandComponents.PLAYER_DATA.get(player).addIsland(ownerName);
                    }
                } else {
                    player.sendMessage(Texts.prefixed("message.skylands.accept.fail"));
                }
            } else {
                player.sendMessage(Texts.prefixed("message.skylands.accept.no_island"));
            }
        } else {
            player.sendMessage(Texts.prefixed("message.skylands.accept.no_player"));
        }
    }
}
