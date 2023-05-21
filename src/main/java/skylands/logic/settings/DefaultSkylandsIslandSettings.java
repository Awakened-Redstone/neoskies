package skylands.logic.settings;

import net.minecraft.item.Item;
import skylands.SkylandsMain;

@Deprecated //Replace with builder
public class DefaultSkylandsIslandSettings extends IslandSettings {
    public DefaultSkylandsIslandSettings(String path, Item icon) {
        super(SkylandsMain.id(path), icon);
    }
}
