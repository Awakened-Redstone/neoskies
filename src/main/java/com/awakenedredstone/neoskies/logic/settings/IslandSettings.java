package com.awakenedredstone.neoskies.logic.settings;

import com.awakenedredstone.neoskies.gui.polymer.CBGuiElement;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import com.awakenedredstone.neoskies.api.island.IslandSettingsManager;
import com.awakenedredstone.neoskies.logic.registry.SkylandsPermissionLevels;

public abstract class IslandSettings {
    private final Identifier identifier;
    private final CBGuiElement icon;
    private final boolean silent;

    public IslandSettings(Identifier identifier, CBGuiElement icon) {
        this(identifier, icon, false);
    }

    public IslandSettings(Identifier identifier, CBGuiElement icon, boolean silent) {
        this.identifier = identifier;
        this.icon = icon;
        this.silent = silent;
        register();
    }

    public IslandSettings(Identifier identifier, Item icon) {
        this(identifier, new CBGuiElementBuilder(icon).build());
    }

    public IslandSettings(Identifier identifier, Item icon, boolean silent) {
        this(identifier, new CBGuiElementBuilder(icon).build(), silent);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public CBGuiElement getIcon() {
        return icon;
    }

    public boolean isSilent() {
        return silent;
    }

    public String getTranslationKey() {
        return identifier.toTranslationKey();
    }

    protected void register() {
        IslandSettingsManager.register(identifier, new com.awakenedredstone.neoskies.api.island.IslandSettings(SkylandsPermissionLevels.MEMBER), icon);
    }
}
