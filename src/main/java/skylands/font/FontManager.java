package skylands.font;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import skylands.SkylandsMain;
import skylands.util.PreInitData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@ApiStatus.Internal
@ApiStatus.Experimental
public class FontManager implements SimpleSynchronousResourceReloadListener {

    public static final FontManager INSTANCE = new FontManager();

    public List<FontProvider> fontProviders = null;

    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(FontManager.INSTANCE);
    }

    @Override
    public Identifier getFabricId() {
        return SkylandsMain.id("fonts");
    }

    @Override
    public void reload(ResourceManager manager) {
        if (fontProviders == null) {
            if (PreInitData.open()) PreInitData.getInstance().setResourceManager(manager);
            for (Identifier id : manager.findResources("font", path -> path.getPath().endsWith(".glyphs.json")).keySet()) {
                try (InputStream stream = manager.getResource(id).get().getInputStream(); Reader reader = new InputStreamReader(stream, "UTF-8");) {
                    fontProviders = FontProvider.LIST_CODEC.parse(JsonOps.INSTANCE, SkylandsMain.GSON.fromJson(reader, JsonElement.class)).resultOrPartial(SkylandsMain.LOGGER::error).orElseThrow();
                } catch (Exception e) {
                    SkylandsMain.LOGGER.error("Error occurred while loading the fonts data for " + id.toString(), e);
                }
            }
        }
    }
}
