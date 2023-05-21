package skylands.api.island;

import lombok.Getter;
import net.minecraft.util.Identifier;
import skylands.logic.SkylandsRegistries;

public abstract class PermissionLevel {
    @Getter(lazy = true)
    private final Identifier id = getIdentifierFromRegistry();
    @Getter
    private final int level;

    public PermissionLevel(int level) {
        this.level = level;
    }

    private Identifier getIdentifierFromRegistry() {
        return SkylandsRegistries.PERMISSION_LEVELS.getId(this);
    }

    public static PermissionLevel fromValue(Identifier id) {
        return SkylandsRegistries.PERMISSION_LEVELS.get(id);
    }

    public static PermissionLevel fromValue(String id) {
        return SkylandsRegistries.PERMISSION_LEVELS.get(new Identifier(id));
    }
}
