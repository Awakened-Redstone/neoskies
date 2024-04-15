package com.awakenedredstone.neoskies.util;

import net.minecraft.resource.ResourceManager;

public class PreInitData {
    private static boolean open = true;
    private static PreInitData instance = new PreInitData();
    private ResourceManager resourceManager;

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public static PreInitData getInstance() {
        return instance;
    }

    public static void close() {
        instance = null;
        open = false;
    }

    public static boolean open() {
        return open;
    }
}
