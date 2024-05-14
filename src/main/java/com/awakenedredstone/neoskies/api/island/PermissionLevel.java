package com.awakenedredstone.neoskies.api.island;

import com.awakenedredstone.neoskies.logic.registry.NeoSkiesRegistries;
import net.minecraft.util.Identifier;

import java.util.concurrent.atomic.AtomicReference;

public abstract class PermissionLevel {
    private final AtomicReference<Identifier> id = new AtomicReference<>();
    private final int level;

    public PermissionLevel(int level) {
        this.level = level;
    }

    public Identifier getId() {
        if (id.get() == null) {
            id.set(getIdentifierFromRegistry());
        }
        return id.get();
    }

    public int getLevel() {
        return level;
    }

    private Identifier getIdentifierFromRegistry() {
        return NeoSkiesRegistries.PERMISSION_LEVELS.getId(this);
    }

    public static PermissionLevel fromValue(Identifier id) {
        return NeoSkiesRegistries.PERMISSION_LEVELS.get(id);
    }

    public static PermissionLevel fromValue(String id) {
        return NeoSkiesRegistries.PERMISSION_LEVELS.get(new Identifier(id));
    }
}
