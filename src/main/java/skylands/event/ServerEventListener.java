package skylands.event;

import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.server.MinecraftServer;
import skylands.SkylandsMain;
import skylands.logic.Island;
import skylands.logic.Skylands;
import skylands.util.PreInitData;

import java.util.Optional;

public class ServerEventListener {

    public static void onTick(MinecraftServer server) {
        Skylands.getInstance().onTick(server);
    }

    public static void onStart(MinecraftServer server) {
        Skylands.init(server);
        PreInitData.close();
        CommonEconomy.register("skylands", Skylands.getInstance().economy.PROVIDER);
        registerPlaceholders();
    }

    public static void onStop(MinecraftServer server) {
        Skylands.getInstance().close();
    }

    //TODO: Add placeholders
    private static void registerPlaceholders() {
        Placeholders.register(SkylandsMain.id("locked"), (context, argument) -> {
            Optional<Island> island = Skylands.getInstance().islands.getByPlayer(context.player());
            return island.map(value -> PlaceholderResult.value(value.locked ? "Locked" : "Open")).orElseGet(() -> PlaceholderResult.invalid("Missing island!"));
        });

        Placeholders.register(SkylandsMain.id("size"), (context, argument) -> {
            Optional<Island> island = Skylands.getInstance().islands.getByPlayer(context.player());
            return island.map(value -> PlaceholderResult.value(String.valueOf(value.radius * 2 + 1))).orElseGet(() -> PlaceholderResult.invalid("Missing island!"));
        });
    }
}
