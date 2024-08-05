package com.awakenedredstone.neoskies.event;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.PreInitData;
import com.awakenedredstone.neoskies.util.Texts;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.server.MinecraftServer;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ServerEventListener {
    public static void onTick(MinecraftServer server) {
        IslandLogic.getInstance().onTick(server);
    }

    public static void onStart(MinecraftServer server) {
        IslandLogic.init(server);
        PreInitData.close();
        //CommonEconomy.register("neoskies", IslandLogic.getInstance().ECONOMY.PROVIDER);
        registerPlaceholders();
    }

    public static void onStop(MinecraftServer server) {
        IslandLogic.getInstance().close();
    }

    //TODO: Add placeholders
    private static void registerPlaceholders() {
        String missing = Texts.translatable("neoskies.island.placeholders.missing_island").getString();
        PlaceholderResult invalid = PlaceholderResult.invalid(missing);

        Placeholders.register(NeoSkies.id("locked"), (context, argument) -> {
            Optional<Island> island = IslandLogic.getInstance().islands.getByPlayer(context.player());
            return island.map(value -> PlaceholderResult.value(value.locked ? "Locked" : "Open")).orElse(invalid);
        });

        Placeholders.register(NeoSkies.id("size"), (context, argument) -> {
            Optional<Island> island = IslandLogic.getInstance().islands.getByPlayer(context.player());
            return island.map(value -> PlaceholderResult.value(String.valueOf(value.radius * 2 + 1))).orElse(invalid);
        });

        Placeholders.register(NeoSkies.id("id"), (context, argument) -> {
            Optional<Island> islandOptional = IslandLogic.getInstance().islands.getByPlayer(context.player());
            return islandOptional.map(island -> PlaceholderResult.value(island.getIslandId().toString())).orElse(invalid);
        });

        Placeholders.register(NeoSkies.id("owner"), (context, argument) -> {
            Optional<Island> islandOptional = IslandLogic.getInstance().islands.getByPlayer(context.player());
            return islandOptional.map(island -> PlaceholderResult.value(island.owner.name)).orElse(invalid);
        });

        Placeholders.register(NeoSkies.id("scanning"), (context, argument) -> {
            Optional<Island> islandOptional = IslandLogic.getInstance().islands.getByPlayer(context.player());
            return islandOptional.map(island -> PlaceholderResult.value(Texts.translatable(island.isScanning() ? "neoskies.island.scanning" : "neoskies.island.not_scanning"))).orElse(invalid);
        });

        Placeholders.register(NeoSkies.id("raw/scanning"), (context, argument) -> {
            Optional<Island> islandOptional = IslandLogic.getInstance().islands.getByPlayer(context.player());
            return islandOptional.map(island -> PlaceholderResult.value(String.valueOf(island.isScanning()))).orElse(invalid);
        });

        Placeholders.register(NeoSkies.id("creation_date"), (context, argument) -> {
            Optional<Island> islandOptional = IslandLogic.getInstance().islands.getByPlayer(context.player());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());
            return islandOptional.map(island -> PlaceholderResult.value(formatter.format(island.getCreated()))).orElse(invalid);
        });

        Placeholders.register(NeoSkies.id("wallet"), (context, argument) -> {
            Optional<Island> islandOptional = IslandLogic.getInstance().islands.getByPlayer(context.player());
            return islandOptional.map(island -> PlaceholderResult.value(island.getWallet().formattedBalance())).orElse(invalid);
        });

        Placeholders.register(NeoSkies.id("points"), (context, argument) -> {
            Optional<Island> islandOptional = IslandLogic.getInstance().islands.getByPlayer(context.player());
            return islandOptional.map(island -> PlaceholderResult.value(String.valueOf(island.getPoints()))).orElse(invalid);
        });
    }
}
