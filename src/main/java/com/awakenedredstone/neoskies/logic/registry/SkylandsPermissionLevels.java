package com.awakenedredstone.neoskies.logic.registry;

import com.awakenedredstone.neoskies.SkylandsMain;
import net.minecraft.registry.Registry;
import com.awakenedredstone.neoskies.api.island.DefaultPermissionLevel;
import com.awakenedredstone.neoskies.api.island.PermissionLevel;
import com.awakenedredstone.neoskies.logic.SkylandsRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class SkylandsPermissionLevels {
    public static final PermissionLevel OWNER = new DefaultPermissionLevel(99);
    public static final PermissionLevel MEMBER = new DefaultPermissionLevel(5);
    public static final PermissionLevel VISITOR = new DefaultPermissionLevel(0);

    public static void init() {
        Class<SkylandsPermissionLevels> clazz = SkylandsPermissionLevels.class;
        PermissionLevel settings;
        for (Field field : clazz.getDeclaredFields()) {
            try {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers)) continue;
                boolean access = field.canAccess(null);
                if (!access) field.setAccessible(true);

                if (field.getType() == PermissionLevel.class) {
                    settings = (PermissionLevel) field.get(null);
                    Registry.register(SkylandsRegistries.PERMISSION_LEVELS, SkylandsMain.id(field.getName().toLowerCase()), settings);
                }

                if (!access) field.setAccessible(false);
            } catch (IllegalAccessException e) {
                SkylandsMain.LOGGER.error("Failed to register island settings", e);
            }
        }
    }
}