package com.awakenedredstone.neoskies.logic.settings;

import net.minecraft.item.Item;
import com.awakenedredstone.neoskies.SkylandsMain;

@Deprecated //TODO: Replace with builder
public class DefaultNeoSkiesIslandSettings extends IslandSettings {
    public DefaultNeoSkiesIslandSettings(String path, Item icon) {
        super(SkylandsMain.id(path), icon);
    }

    public DefaultNeoSkiesIslandSettings(String path, Item icon, boolean silent) {
        super(SkylandsMain.id(path), icon, silent);
    }
}
