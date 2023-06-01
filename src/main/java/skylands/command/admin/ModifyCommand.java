package skylands.command.admin;

import com.awakenedredstone.cbserverconfig.util.MapBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import skylands.api.SkylandsAPI;
import skylands.command.utils.CommandUtils;
import skylands.logic.Island;
import skylands.util.Texts;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static skylands.command.utils.CommandUtils.*;

public class ModifyCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        registerAdmin(dispatcher, adminNode()
            .then(literal("modify").requires(Permissions.require("skylands.admin.modify", 4))
                .then(argument("island", StringArgumentType.word())
                    .suggests(CommandUtils.ISLAND_SUGGESTIONS)
                    .then(literal("size")
                        .then(argument("size", IntegerArgumentType.integer(1, 1000))
                            .executes(context -> {
                                String islandId = StringArgumentType.getString(context, "island");
                                int size = IntegerArgumentType.getInteger(context, "size");
                                return modifyIslandSize(context.getSource(), SkylandsAPI.getIsland(UUID.fromString(islandId)).orElse(null), size);
                            })
                        )
                    )
                )
            )
        );
    }

    private static int modifyIslandSize(ServerCommandSource source, @Nullable Island island, int size) {
        if (!assertIsland(source, island)) return 0;
        island.radius = size;

        source.sendFeedback(Texts.of(Texts.of("message.skylands.island.modify.size", new MapBuilder.StringMap()
            .put("player", island.owner.name)
            .putAny("size", size)
            .build())), true);

        return 0;
    }
}
