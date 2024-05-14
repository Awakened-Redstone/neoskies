package com.awakenedredstone.neoskies.logic.settings;

import com.awakenedredstone.neoskies.api.island.CurrentSettings;
import com.awakenedredstone.neoskies.api.island.PermissionLevel;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesRegistries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IslandSettingsUtil {
    public static void offsetPermission(CurrentSettings settings, int offset) {
        int position = 0;
        List<PermissionLevel> levels = NeoSkiesRegistries.PERMISSION_LEVELS.streamEntries().map(RegistryEntry.Reference::value).toList();
        int length = levels.size();
        for (int i = 0; i < length; i++) {
            if (levels.get(i) == settings.getPermissionLevel()) {
                position = i;
                break;
            }
        }

        position += offset;
        while (position < 0) {
            position += length;
        }

        while (position >= length) {
            position -= length;
        }

        settings.setPermissionLevel(levels.get(position));
    }

    @Nullable
    public static CurrentSettings getModifiable(Identifier identifier) {
        return getModifiable(NeoSkiesRegistries.ISLAND_SETTINGS.get(identifier));
    }

    @Nullable
    public static CurrentSettings getModifiable(IslandSettings settings) {
        if (settings == null) return null;
        return new CurrentSettings(settings, settings.getDefaultLevel());
    }
}
