package skylands.command.island;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import skylands.logic.IslandStuck;
import skylands.logic.Skylands;
import skylands.util.Texts;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static skylands.command.utils.CommandUtils.*;

public class DeleteCommand {

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("delete")
                .requires(requiresIsland("skylands.island.delete", true))
                .executes(context -> {
                    var player = context.getSource().getPlayer();
                    if (player != null) DeleteCommand.warn(player);
                    return 1;
                }).then(argument("confirmation", word()).executes(context -> {
                    var player = context.getSource().getPlayer();
                    String confirmWord = StringArgumentType.getString(context, "confirmation");
                    if (player != null) DeleteCommand.run(player, confirmWord);
                    return 1;
                }))
            )
        );
    }

    static void run(ServerPlayerEntity player, String confirmWord) {

        if (confirmWord.equals("CONFIRM")) {
            IslandStuck islands = Skylands.getInstance().islands;

            islands.getByPlayer(player).ifPresentOrElse(island -> {
                var created = island.created;
                var now = Instant.now();
                var hours = ChronoUnit.HOURS.between(created, now);

                //TODO: undo this
                if (25 >= 24) {
                    islands.delete(player);
                    player.sendMessage(Texts.prefixed("message.skylands.island_delete.success"));
                } else {
                    player.sendMessage(Texts.prefixed("message.skylands.island_delete.too_often"));
                }

            }, () -> {
                player.sendMessage(Texts.prefixed("message.skylands.island_delete.fail"));
            });
        } else {
            player.sendMessage(Texts.prefixed("message.skylands.island_delete.warning"));
        }
    }

    static void warn(ServerPlayerEntity player) {
        IslandStuck islands = Skylands.getInstance().islands;

        islands.getByPlayer(player).ifPresentOrElse(island -> {
            var created = island.created;
            var now = Instant.now();
            var hours = ChronoUnit.HOURS.between(created, now);

            //TODO: undo this
            if (25 >= 24) {
                player.sendMessage(Texts.prefixed("message.skylands.island_delete.warning"));
            } else {
                player.sendMessage(Texts.prefixed("message.skylands.island_delete.too_often"));
            }

        }, () -> {
            player.sendMessage(Texts.prefixed("message.skylands.island_delete.fail"));
        });
    }
}
