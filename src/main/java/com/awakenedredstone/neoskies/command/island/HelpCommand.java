package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Collection;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static net.minecraft.server.command.CommandManager.literal;

public class HelpCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node()
            .then(literal("help")
                .requires(Permissions.require("neoskies.command.help", true))
                .executes(context -> {
                    HelpCommand.run(context.getSource(), dispatcher);
                    return 1;
                })
            )
        );
    }

    private static void run(ServerCommandSource source, CommandDispatcher<ServerCommandSource> dispatcher) {
        ParseResults<ServerCommandSource> parseResults = dispatcher.parse(IslandLogic.getConfig().commands.command, source);
        if (parseResults.getContext().getNodes().isEmpty()) {
            source.sendError(Texts.translatable("commands.neoskies.error.no_commands"));
            return;
        }

        //Map<CommandNode<ServerCommandSource>, String> nodes2 = dispatcher.getSmartUsage(Iterables.getLast(parseResults.getContext().getNodes()).getNode(), source);

        CommandNode<ServerCommandSource> node = Iterables.getLast(parseResults.getContext().getNodes()).getNode();
        Collection<CommandNode<ServerCommandSource>> nodes = node.getChildren().stream().sorted().toList();
        source.sendFeedback(() -> Texts.translatable("commands.neoskies.help"), false);
        sendCommands(nodes, source, "", "");
    }

    private static boolean sendCommands(Collection<CommandNode<ServerCommandSource>> nodes, ServerCommandSource source, String parent, String parentTranslation) {
        for (CommandNode<ServerCommandSource> node : nodes) {
            if (node.getChildren().isEmpty() || node.getCommand() != null) {
                String command = node.getUsageText();
                MutableText prefix = Texts.translatable("commands.neoskies.help.prefix", map -> {
                    map.put("prefix", IslandLogic.getConfig().commands.command);
                    map.put("command", parent + command);
                });
                String string = "commands.description.neoskies." + parentTranslation + command;
                Text description = Texts.literal(string.replaceAll("\\.<.*>$", ""));
                source.sendFeedback(() -> prefix.append(description), false);
            }
            sendCommands(node.getChildren(), source, parent + node.getUsageText() + " ", parentTranslation + node.getUsageText() + ".");
        }
        return false;
    }
}
