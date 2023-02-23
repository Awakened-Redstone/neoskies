package skylands.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import skylands.SkylandsMain;

public class Constants {
    public static final String NAMESPACE = SkylandsMain.MOD_ID;
    public static final String NAMESPACE_NETHER = SkylandsMain.MOD_ID + "_nether";
    public static final String NAMESPACE_END = SkylandsMain.MOD_ID + "_end";
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
}
