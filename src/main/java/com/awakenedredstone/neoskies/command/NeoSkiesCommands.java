package com.awakenedredstone.neoskies.command;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.command.admin.*;
import com.awakenedredstone.neoskies.command.island.*;
import com.awakenedredstone.neoskies.command.island.LevelCommand;
import com.awakenedredstone.neoskies.command.utils.CommandUtils;
import com.awakenedredstone.neoskies.gui.CobbleGenGui;
import com.awakenedredstone.neoskies.logic.AdminLevelCommand;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.LinedStringBuilder;
import com.awakenedredstone.neoskies.util.MapBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.UnitConvertions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.adminNode;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.registerAdmin;
import static net.minecraft.server.command.CommandManager.argument;

public class NeoSkiesCommands {
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> NeoSkiesCommands.register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        NeoSkies.LOGGER.debug("Registering commands...");
        registerPublicCommands(dispatcher);
        registerAdminCommands(dispatcher);
    }

    private static void registerPublicCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        MenuCommand.init(dispatcher);
        CreateCommand.init(dispatcher);
        HubCommands.init(dispatcher);
        HomeCommand.init(dispatcher);
        VisitCommand.init(dispatcher);
        MemberCommands.init(dispatcher);
        BanCommands.init(dispatcher);
        KickCommand.init(dispatcher);
        HelpCommand.init(dispatcher);
        AcceptCommand.init(dispatcher);
        DeleteCommand.init(dispatcher);
        SettingCommands.init(dispatcher);
        LevelCommand.init(dispatcher);
    }

    private static void registerAdminCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        DeleteIslandCommand.init(dispatcher);
        SettingsCommand.init(dispatcher);
        BallanceCommand.init(dispatcher);
        IslandDataCommand.init(dispatcher);
        ModifyCommand.init(dispatcher);
        AdminLevelCommand.init(dispatcher);

        registerAdmin(dispatcher, adminNode()
          .then(CommandManager.literal("reload")
            .requires(Permissions.require("neoskies.admin.reload", 4))
            .executes(context -> {
                context.getSource().sendFeedback(() -> Texts.prefixed(Text.translatable("commands.neoskies.reload")), true);
                IslandLogic.getConfig().load();
                IslandLogic.getRankingConfig().load();
                return 1;
            })
          ).then(CommandManager.literal("bypass")
            .requires(Permissions.require("neoskies.admin.protection.bypass", 4))
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                if (!source.isExecutedByPlayer()) {
                    source.sendError(Texts.prefixed(Text.translatable("commands.neoskies.error.player_only")));
                    return 0;
                }

                ServerPlayerEntity player = source.getPlayer();

                Set<PlayerEntity> protectionBypass = NeoSkies.PROTECTION_BYPASS;
                boolean overrideMode = protectionBypass.contains(player);
                if (overrideMode) {
                    protectionBypass.remove(player);
                    source.sendFeedback(() -> Texts.prefixed(Text.translatable("commands.neoskies.admin.bypass.disable")), true);
                } else {
                    protectionBypass.add(player);
                    source.sendFeedback(() -> Texts.prefixed(Text.translatable("commands.neoskies.admin.bypass.enable")), true);
                }
                return 1;
            })
          ).then(CommandManager.literal("list")
            .requires(Permissions.require("neoskies.admin.island.list", 4))
            .executes(context -> {
                LinedStringBuilder builder = new LinedStringBuilder();
                List<Island> islands = IslandLogic.getInstance().islands.stuck;
                for (Island island : islands) {
                    builder.appendLine(island.owner.name, "'s island: ", island.getIslandId().toString());
                }

                context.getSource().sendFeedback(() -> Text.literal(builder.toString()), false);
                return islands.size();
            })
          ).then(CommandManager.literal("cobble")
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                if (!source.isExecutedByPlayer()) {
                    source.sendError(Texts.of("This command must be executed by a player!"));
                    return 0;
                }

                new CobbleGenGui(source.getPlayer()).open();

                return 0;
            })
          )
        );
    }
}
