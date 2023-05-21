package skylands;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonPrimitive;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import skylands.command.SkylandsCommands;
import skylands.config.MainConfigs;
import skylands.data.reloadable.SongsData;
import skylands.event.SkylandsEvents;
import skylands.logic.registry.SkylandsIslandSettings;
import skylands.logic.registry.SkylandsPermissionLevels;

import java.util.HashSet;
import java.util.Set;

public class SkylandsMain implements ModInitializer {
    public static final String MOD_ID = "skylands";
    public static final Logger LOGGER = LoggerFactory.getLogger("Skylands");
    public static final MainConfigs MAIN_CONFIG;
    public static final Set<PlayerEntity> PROTECTION_BYPASS = new HashSet<>();

    //TODO: Add GUIs
    //TODO: Fix end island
    //TODO: Better config system
    //TODO: Simple (and optimised) datapack based island templates
    @Override
    public void onInitialize() {
        FieldRegistrationHandler.register(SkylandsIslandSettings.class, MOD_ID, false);
        FieldRegistrationHandler.register(SkylandsPermissionLevels.class, MOD_ID, false);
        SkylandsEvents.init();
        SkylandsCommands.init();
        SongsData.init();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    static {
        MAIN_CONFIG = MainConfigs.createAndLoad(builder -> {
            builder.registerSerializer(Vec3d.class, (vec3d, marshaller) -> {
                JsonArray array = new JsonArray();
                array.add(new JsonPrimitive(vec3d.getX()));
                array.add(new JsonPrimitive(vec3d.getY()));
                array.add(new JsonPrimitive(vec3d.getZ()));
                return array;
            });

            builder.registerDeserializer(JsonArray.class, Vec3d.class, (json, m) ->
                    new Vec3d(
                            json.getDouble(0, 0),
                            json.getDouble(1, 0),
                            json.getDouble(2, 0)
                    )
            );
        });
    }
}
