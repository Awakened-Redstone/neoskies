package com.awakenedredstone.neoskies.command.admin;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.command.utils.CommandUtils;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.util.MapBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModifyCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        final LiteralArgumentBuilder<ServerCommandSource> gameruleArgumentBuilder = CommandManager.literal("gamerule").requires(source -> source.hasPermissionLevel(2));
        GameRules.accept(new GameRules.Visitor() {

            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                gameruleArgumentBuilder
                  .then(literal(key.getName())
                    .executes(context -> {
                        String islandId = StringArgumentType.getString(context, "island");
                        Island island = NeoSkiesAPI.getIsland(UUID.fromString(islandId)).orElse(null);

                        return getGamerule(context.getSource(), key, island);
                    })
                    .then(type.argument("value")
                      .executes(context -> {
                          String islandId = StringArgumentType.getString(context, "island");
                          Island island = NeoSkiesAPI.getIsland(UUID.fromString(islandId)).orElse(null);

                          return setGamerule(context, key, island);
                      })
                    )
                  );
            }
        });

        registerAdmin(dispatcher, adminNode()
          .then(literal("modify").requires(Permissions.require("neoskies.admin.modify", 4))
            .then(argument("island", StringArgumentType.word())
              .suggests(CommandUtils.ISLAND_SUGGESTIONS)
              .then(literal("size")
                .then(argument("size", IntegerArgumentType.integer())
                  .executes(context -> {
                      String islandId = StringArgumentType.getString(context, "island");
                      int size = IntegerArgumentType.getInteger(context, "size");
                      return modifyIslandSize(context.getSource(), NeoSkiesAPI.getIsland(UUID.fromString(islandId)).orElse(null), size);
                  })
                )
              ).then(gameruleArgumentBuilder)
            )
          )
        );
    }

    private static int modifyIslandSize(ServerCommandSource source, @Nullable Island island, int size) {
        if (!assertIsland(source, island)) return 0;
        island.radius = size;

        source.sendFeedback(() -> Texts.of(Texts.of("message.neoskies.island.modify.size", new MapBuilder.StringMap()
          .put("player", island.owner.name)
          .putAny("size", size)
          .build())), true);

        return size;
    }

    static <T extends GameRules.Rule<T>> int setGamerule(CommandContext<ServerCommandSource> context, GameRules.Key<T> key, @Nullable Island island) {
        ServerCommandSource source = context.getSource();

        if (!assertIsland(source, island)) return 0;

        ServerWorld world = island.getOverworldHandler().asWorld();
        T rule = world.getGameRules().get(key);
        rule.set(context, "value");
        source.sendFeedback(() -> Text.translatable("commands.gamerule.set", key.getName(), rule.toString()), true);
        return rule.getCommandResult();
    }

    static <T extends GameRules.Rule<T>> int getGamerule(ServerCommandSource source, GameRules.Key<T> key, @Nullable Island island) {
        if (!assertIsland(source, island)) return 0;

        ServerWorld world = island.getOverworldHandler().asWorld();
        T rule = world.getGameRules().get(key);
        source.sendFeedback(() -> Text.translatable("commands.gamerule.query", key.getName(), rule.toString()), false);
        return rule.getCommandResult();
    }
}
