package skylands.config;

import io.wispforest.owo.config.annotation.Config;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Config(name = "skylands", wrapperName = "MainConfigs")
public class MainConfig {
    public String language = "en_us";
    public String command = "sl";
    public List<String> commandAliases = new ArrayList<>();
    public String adminCommand = "force-sl";
    public List<String> adminCommandAliases = new ArrayList<>();
    public boolean allowVisitCurrentIsland = false;
    public int defaultIslandRadius = -1;
    //public int deletionCooldown = -1;
    //public int islandLimit = -1;
    //public boolean resetPlayerWithIsland = false;
    public Vec3d defaultIslandLocation = new Vec3d(0.5d, 75d, 0.5d);
    public boolean disableBlocksOutsideIslands = false;
    public boolean disableEntitiesOutsideIslands = false;
    public boolean enableEndIsland = false;
    public boolean safeVoid = false;
    public boolean safeVoidFallDamage = true;
    public byte safeVoidBlocksBelow = 16;
    public boolean showProtectionMessages = true;
}
