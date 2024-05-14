package com.awakenedredstone.neoskies.command;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.command.admin.*;
import com.awakenedredstone.neoskies.command.island.*;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Set;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.adminNode;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.registerAdmin;

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

        registerAdmin(dispatcher, adminNode()
          .then(CommandManager.literal("reload")
            .executes(context -> {
                context.getSource().sendFeedback(() -> Texts.prefixed(Text.translatable("commands.neoskies.reload")), true);
                IslandLogic.getConfig().load();
                return 1;
            })
          )
        );

        registerAdmin(dispatcher, adminNode()
          .then(CommandManager.literal("bypass")
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
          )
        );
    }
}
