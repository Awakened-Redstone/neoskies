package com.awakenedredstone.neoskies.test;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.datagen.NeoSkiesBlockGenProvider;
import com.awakenedredstone.neoskies.util.Texts;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;

public final class NeoSkiesTestClientMain implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("NeoSkies Test Client");
    private static int tick = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (tick++ % 2 == 0) {
                DebugImGuiRenderer.MEMORY.add((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("sbac")
                .then(ClientCommandManager.literal("debug")
                  .executes(context -> {
                      DebugImGuiRenderer.OPEN.set(true);
                      return 0;
                  })
                )
            );
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("datagen").requires(source -> source.hasPermissionLevel(2))
              .executes(context -> {
                  ServerCommandSource source = context.getSource();
                  source.sendFeedback(() -> Texts.literal("Creating datapack"), false);
                  long time = source.getWorld().getTime();
                  Path path = source.getServer().getSavePath(WorldSavePath.DATAPACKS).resolve("test" + time);
                  FabricDataOutput output = new FabricDataOutput(FabricLoader.getInstance().getModContainer(NeoSkies.MOD_ID).get(), path, true);
                  DataProvider provider = new NeoSkiesBlockGenProvider(output, CompletableFuture.completedFuture(registryAccess));
                  provider.run(DataWriter.UNCACHED).whenComplete((o, throwable) -> {
                      try {
                          Files.writeString(path.resolve("pack.mcmeta"), "{\"pack\": {\"pack_format\": 34,\"description\": \"\"}}");
                      } catch (Throwable e) {
                          LOGGER.error("Failed to create pack.mcmeta for \"{}\"", "test" + time, e);
                      }
                      source.sendFeedback(() -> Texts.translatable("Created datapack test" + time), false);
                  });
                  return 0;
              })
            );
        });
    }
}
