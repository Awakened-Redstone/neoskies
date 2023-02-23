package skylands.event;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import skylands.SkylandsMain;
import skylands.logic.Island;
import skylands.logic.Skylands;

import java.util.Optional;

public class ServerStartEvent {

    public static void onStart(MinecraftServer server) {
        Skylands.instance = new Skylands(server);

        ServerMetadata serverMeta = server.getServerMetadata();
        Text motd = serverMeta.getDescription();

        if (motd == null || motd.getString().equals("A Minecraft Server")) {
            FabricLoader.getInstance().getModContainer("skylands").ifPresent(mod -> {
                var modMeta = mod.getMetadata();
                serverMeta.setDescription(Text.of("Skylands Alpha Build " + modMeta.getVersion().getFriendlyString()));
            });
        }

        registerPlaceholders();
    }

    //TODO: Add placeholders
    private static void registerPlaceholders() {
        Placeholders.register(SkylandsMain.id("locked"), (context, argument) -> {
            Optional<Island> island = Skylands.instance.islands.get(context.player());
            return island.map(value -> PlaceholderResult.value(value.locked ? "Locked" : "Open")).orElseGet(() -> PlaceholderResult.invalid("Missing island!"));
        });

        Placeholders.register(SkylandsMain.id("size"), (context, argument) -> {
            Optional<Island> island = Skylands.instance.islands.get(context.player());
            return island.map(value -> PlaceholderResult.value(String.valueOf(value.radius * 2 + 1))).orElseGet(() -> PlaceholderResult.invalid("Missing island!"));
        });
    }
}
