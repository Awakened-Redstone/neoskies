package skylands.command;

import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import skylands.logic.Island;
import skylands.logic.IslandStuck;
import skylands.logic.Skylands;
import skylands.util.Texts;

import static net.minecraft.server.command.CommandManager.literal;
import static skylands.command.utils.CommandUtils.node;
import static skylands.command.utils.CommandUtils.register;

public class CreateCommand {

    static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node().then(literal("create").requires(Permissions.require("skylands.island.create", true))
                .executes(context -> {
            var source = context.getSource();
            var player = source.getPlayer();
            if (player != null) {
                CreateCommand.run(player);
            }
            return 1;
        })));
    }

    static void run(ServerPlayerEntity player) {
        IslandStuck islands = Skylands.instance.islands;

        if (islands.get(player).isPresent()) {
            player.sendMessage(Texts.prefixed("message.skylands.island_create.fail"));
        } else {
            Island island = islands.create(player);
            island.onFirstLoad();
            player.sendMessage(Texts.prefixed("message.skylands.island_create.success"));
        }
    }
}
