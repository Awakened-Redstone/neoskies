package skylands.logic.registry;

import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.registry.Registry;
import skylands.api.island.DefaultPermissionLevel;
import skylands.api.island.PermissionLevel;
import skylands.logic.SkylandsRegistries;

public class SkylandsPermissionLevels implements AutoRegistryContainer<PermissionLevel> {
    public static final PermissionLevel OWNER = new DefaultPermissionLevel(99);
    public static final PermissionLevel MEMBER = new DefaultPermissionLevel(5);
    public static final PermissionLevel VISITOR = new DefaultPermissionLevel(0);

    @Override
    public Registry<PermissionLevel> getRegistry() {
        return SkylandsRegistries.PERMISSION_LEVELS;
    }

    @Override
    public Class<PermissionLevel> getTargetFieldType() {
        return PermissionLevel.class;
    }
}
