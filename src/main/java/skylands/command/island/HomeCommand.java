package skylands.command.island;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import skylands.SkylandsMain;
import skylands.data.SkylandComponents;
import skylands.util.Texts;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static skylands.command.utils.CommandUtils.node;
import static skylands.command.utils.CommandUtils.register;

public class HomeCommand {

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("home").requires(Permissions.require("skylands.teleport.home", true))
                .executes(context -> {
                    var player = context.getSource().getPlayer();
                    if (player != null) {
                        HomeCommand.run(player);
                    }
                    return 1;
                }).then(argument("player", word())
                    .requires(Permissions.require("skylands.teleport.home.other", true))
                    .suggests((context, builder) -> {
                        var player = context.getSource().getPlayer();

                        if (player != null) {
                            var islands = SkylandComponents.PLAYER_DATA.get(player).getIslands();

                            String remains = builder.getRemaining();

                            for (String ownerName : islands) {
                                if (ownerName.contains(remains)) {
                                    builder.suggest(ownerName);
                                }
                            }
                            return builder.buildFuture();
                        }
                        return builder.buildFuture();
                    }).executes(context -> {
                        var ownerName = StringArgumentType.getString(context, "player");
                        var visitor = context.getSource().getPlayer();
                        if (visitor != null) {
                            HomeCommand.run(visitor, ownerName);
                        }
                        return 1;
                    })
                )
            )
        );
    }

    static void run(ServerPlayerEntity player) {
        skylands.logic.Skylands.getInstance().islands.getByPlayer(player).ifPresentOrElse(island -> {
            if (player.getWorld().getRegistryKey().getValue().equals(SkylandsMain.id(player.getUuid().toString())) && !SkylandsMain.MAIN_CONFIG.allowVisitCurrentIsland()) {
                player.sendMessage(Texts.prefixed("message.skylands.home.fail"));
            } else {
                player.sendMessage(Texts.prefixed("message.skylands.home.success"));
                island.visitAsMember(player);
            }
        }, () -> player.sendMessage(Texts.prefixed("message.skylands.home.no_island")));
    }

    static void run(ServerPlayerEntity visitor, String islandOwner) {
        skylands.logic.Skylands.getInstance().islands.getByPlayer(islandOwner).ifPresentOrElse(island -> {
            if (visitor.getWorld().getRegistryKey().getValue().equals(SkylandsMain.id(island.owner.uuid.toString())) && !SkylandsMain.MAIN_CONFIG.allowVisitCurrentIsland()) {
                visitor.sendMessage(Texts.prefixed("message.skylands.visit_home.fail", map -> map.put("owner", islandOwner)));
            } else {
                if (island.isMember(visitor)) {
                    visitor.sendMessage(Texts.prefixed("message.skylands.visit_home.success", map -> map.put("owner", islandOwner)));
                    island.visitAsMember(visitor);
                    SkylandComponents.PLAYER_DATA.get(visitor).addIsland(islandOwner);
                } else {
                    visitor.sendMessage(Texts.prefixed("message.skylands.visit_home.not_member"));
                    SkylandComponents.PLAYER_DATA.get(visitor).removeIsland(islandOwner);
                }
            }
        }, () -> {
            visitor.sendMessage(Texts.prefixed("message.skylands.visit_home.no_island"));
            SkylandComponents.PLAYER_DATA.get(visitor).removeIsland(islandOwner);
        });
    }
}
