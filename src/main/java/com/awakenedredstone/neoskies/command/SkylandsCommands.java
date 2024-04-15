package com.awakenedredstone.neoskies.command;

import com.awakenedredstone.neoskies.config.MainConfig;
import com.awakenedredstone.neoskies.logic.Skylands;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import com.awakenedredstone.neoskies.SkylandsMain;
import com.awakenedredstone.neoskies.command.admin.*;
import com.awakenedredstone.neoskies.command.island.*;
import net.minecraft.text.Text;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.adminNode;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.registerAdmin;

public class SkylandsCommands {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SkylandsCommands.register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        SkylandsMain.LOGGER.debug("Registering commands...");
        registerPublicCommands(dispatcher);
        registerAdminCommands(dispatcher);
    }

    private static void registerPublicCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        //TODO: Fix soft-lock on "/help is"
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
                context.getSource().sendFeedback(() -> Text.translatable("message.neoskies.reload"), true);
                Skylands.getConfig().load();
                return 1;
            })
          )
        );
    }
}
