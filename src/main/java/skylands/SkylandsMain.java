package skylands;

import com.awakenedredstone.cbserverconfig.api.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import skylands.command.SkylandsCommands;
import skylands.config.MainConfig;
import skylands.data.reloadable.SongsData;
import skylands.event.SkylandsEvents;

import java.util.HashSet;
import java.util.Set;

public class SkylandsMain implements ModInitializer {
    public static final String MOD_ID = "skylands";
    public static final Logger LOGGER = LoggerFactory.getLogger("Skylands");
    public static final ConfigManager<MainConfig> MAIN_CONFIG = ConfigManager.register(SkylandsMain.id("skylands"), MainConfig.class, true);
    public static final Set<PlayerEntity> PROTECTION_BYPASS = new HashSet<>();

    //TODO: Support permissions
    //TODO: Add GUIs
    //TODO: Fix end island
    @Override
    public void onInitialize() {
        SkylandsEvents.init();
        SkylandsCommands.init();
        SongsData.init();
    }

    public static MainConfig getConfig() {
        return MAIN_CONFIG.getConfig();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
