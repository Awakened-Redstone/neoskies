package skylands.config;

import com.awakenedredstone.cbserverconfig.annotation.Name;
import com.awakenedredstone.cbserverconfig.api.config.Config;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

@Name("Skylands")
public class MainConfig extends Config {
    public String command = "sl";
    public List<String> commandAliases = new ArrayList<>();
    public String adminCommand = "force-sl";
    public List<String> adminCommandAliases = new ArrayList<>();
    public boolean allowVisitCurrentIsland = false;
    public int defaultIslandRadius = -1;
    //public int deletionCooldown = -1;
    //public int islandLimit = -1;
    //public int resetPlayerWithIsland = -1;
    @JsonAdapter(ConfigJsonAdapters.Vec3dAdapter.class)
    public Vec3d defaultIslandLocation = new Vec3d(0.5D, 75D, 0.5D);
    public boolean disableBlocksOutsideIslands = false;
    public boolean disableEntitiesOutsideIslands = false;
    public boolean enableEndIsland = false;
    public boolean safeVoid = false;
    public boolean safeVoidFallDamage = true;
    public byte safeVoidBlocksBelow = 16;
    public boolean showProtectionMessages = true;
}
