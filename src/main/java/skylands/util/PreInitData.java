package skylands.util;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.resource.ResourceManager;

public class PreInitData {
    private static boolean open = true;
    private static PreInitData instance = new PreInitData();
    @Getter @Setter
    private ResourceManager resourceManager;

    public static PreInitData getInstance() {
        return instance;
    }

    public static void close() {
        instance = null;
        open = false;
    }

    public static boolean open() {
        return open;
    }
}
