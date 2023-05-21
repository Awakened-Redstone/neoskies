package skylands.logic.settings;

import com.awakenedredstone.cbserverconfig.polymer.CBGuiElement;
import com.awakenedredstone.cbserverconfig.polymer.CBGuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import skylands.api.island.SettingsManager;
import skylands.logic.registry.SkylandsPermissionLevels;

public abstract class IslandSettings {
    private final Identifier identifier;
    private final CBGuiElement icon;

    public IslandSettings(Identifier identifier, CBGuiElement icon) {
        this.identifier = identifier;
        this.icon = icon;
        register();
    }

    public IslandSettings(Identifier identifier, Item icon) {
        this(identifier, new CBGuiElementBuilder(icon).build());
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public CBGuiElement getIcon() {
        return icon;
    }

    protected void register() {
        SettingsManager.register(identifier, new skylands.api.island.IslandSettings(SkylandsPermissionLevels.MEMBER), icon);
    }
}
