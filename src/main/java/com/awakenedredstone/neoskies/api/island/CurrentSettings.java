package com.awakenedredstone.neoskies.api.island;

import com.awakenedredstone.neoskies.logic.settings.IslandSettings;

public class CurrentSettings {
    protected final IslandSettings settings;
    protected PermissionLevel permissionLevel;

    public CurrentSettings(IslandSettings settings, PermissionLevel permissionLevel) {
        this.settings = settings;
        this.permissionLevel = permissionLevel;
    }

    public IslandSettings getSettings() {
        return settings;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
    }
}
