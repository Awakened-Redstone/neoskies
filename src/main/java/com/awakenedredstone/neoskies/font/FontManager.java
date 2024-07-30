package com.awakenedredstone.neoskies.font;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.util.PreInitData;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@ApiStatus.Internal
@ApiStatus.Experimental
@Deprecated
public class FontManager implements SimpleSynchronousResourceReloadListener {
    public static final FontManager INSTANCE = new FontManager();

    public List<FontProvider> fontProviders = null;

    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(FontManager.INSTANCE);
    }

    @Override
    public Identifier getFabricId() {
        return NeoSkies.id("fonts");
    }

    @Override
    public void reload(ResourceManager manager) {
        if (fontProviders == null) {
            if (PreInitData.open()) PreInitData.getInstance().setResourceManager(manager);
            for (Identifier id : manager.findResources("font", path -> path.getPath().endsWith(".glyphs.json")).keySet()) {
                try (InputStream stream = manager.getResource(id).get().getInputStream(); Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    fontProviders = FontProvider.LIST_CODEC.parse(JsonOps.INSTANCE, NeoSkies.GSON.fromJson(reader, JsonElement.class)).resultOrPartial(NeoSkies.LOGGER::error).orElseThrow();
                } catch (Exception e) {
                    NeoSkies.LOGGER.error("Error occurred while loading the fonts data for " + id.toString(), e);
                }
            }
        }
    }
}
