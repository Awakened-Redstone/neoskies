package com.awakenedredstone.neoskies.command.admin;

import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.command.utils.CommandUtils;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.util.MapBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyTransaction;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.adminNode;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.registerAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BallanceCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        registerAdmin(dispatcher, adminNode()
            .then(literal("balance").requires(Permissions.require("neoskies.admin.economy.modify", 4))
                .then(argument("island", StringArgumentType.word())
                    .suggests(CommandUtils.ISLAND_SUGGESTIONS)
                    .then(literal("get")
                        .executes(context -> {
                            String islandId = StringArgumentType.getString(context, "island");
                            return getBalance(context.getSource(), NeoSkiesAPI.getOptionalIsland(UUID.fromString(islandId)).orElse(null));
                        })
                    ).then(literal("set")
                        .then(argument("amount", LongArgumentType.longArg())
                            .executes(context -> {
                                String islandId = StringArgumentType.getString(context, "island");
                                long amount = LongArgumentType.getLong(context, "amount");
                                return setBalance(context.getSource(), NeoSkiesAPI.getOptionalIsland(UUID.fromString(islandId)).orElse(null), amount);
                            })
                        )
                    ).then(literal("add")
                        .then(argument("amount", LongArgumentType.longArg())
                            .executes(context -> {
                                String islandId = StringArgumentType.getString(context, "island");
                                long amount = LongArgumentType.getLong(context, "amount");
                                return addBalance(context.getSource(), NeoSkiesAPI.getOptionalIsland(UUID.fromString(islandId)).orElse(null), amount);
                            })
                        )
                    ).then(literal("remove")
                        .then(argument("amount", LongArgumentType.longArg())
                            .executes(context -> {
                                String islandId = StringArgumentType.getString(context, "island");
                                long amount = LongArgumentType.getLong(context, "amount");
                                return removeBalance(context.getSource(), NeoSkiesAPI.getOptionalIsland(UUID.fromString(islandId)).orElse(null), amount);
                            })
                        )
                    )
                )
            )
        );
    }

    private static @Nullable EconomyAccount getWallet(ServerCommandSource source, Island island) {
        if (island == null) {
            source.sendError(Texts.translatable("message.neoskies.error.island_not_found"));
            return null;
        }
        EconomyAccount islandWallet = NeoSkiesAPI.getIslandWallet(island);
        if (islandWallet == null) {
            source.sendError(Texts.translatable("message.neoskies.error.island_wallet_not_found"));
            return null;
        }
        return islandWallet;
    }

    private static int getBalance(ServerCommandSource source, @Nullable Island island) {
        EconomyAccount wallet = getWallet(source, island);
        if (wallet == null) return 0;
        source.sendFeedback(() -> Texts.translatable("message.neoskies.balance.get", map -> new MapBuilder.StringMap()
            .put("island", island.getIslandId().toString())
            .putAny("amount", wallet.balance())), true);
        return 1;
    }

    private static int setBalance(ServerCommandSource source, @Nullable Island island, long amount) {
        EconomyAccount wallet = getWallet(source, island);
        if (wallet == null) return 0;
        wallet.setBalance(amount);
        source.sendFeedback(() -> Texts.translatable("message.neoskies.balance.set", map -> new MapBuilder.StringMap()
            .put("island", island.getIslandId().toString())
            .putAny("amount", amount)), true);
        return 1;
    }

    private static int addBalance(ServerCommandSource source, @Nullable Island island, long amount) {
        EconomyAccount wallet = getWallet(source, island);
        if (wallet == null) return 0;

        EconomyTransaction transaction = wallet.canIncreaseBalance(amount);
        source.sendFeedback(transaction::message, true);
        if (!transaction.isSuccessful()) return 0;
        wallet.increaseBalance(amount);
        return 1;
    }

    private static int removeBalance(ServerCommandSource source, @Nullable Island island, long amount) {
        EconomyAccount wallet = getWallet(source, island);
        if (wallet == null) return 0;

        EconomyTransaction transaction = wallet.canDecreaseBalance(amount);
        source.sendFeedback(transaction::message, true);
        if (!transaction.isSuccessful()) return 0;
        wallet.decreaseBalance(amount);
        return 1;
    }
}
