package skylands.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;

public class ConfigJsonAdapters {

    public static class Vec3dAdapter extends TypeAdapter<Vec3d> {
        @Override
        public void write(JsonWriter out, Vec3d value) throws IOException {
            out.beginArray();
            out.value(value.getX());
            out.value(value.getY());
            out.value(value.getZ());
            out.endArray();
        }

        @Override
        public Vec3d read(JsonReader in) throws IOException {
            in.beginArray();
            Vec3d vec3d = new Vec3d(in.nextDouble(), in.nextDouble(), in.nextDouble());
            in.endArray();
            return vec3d;
        }
    }
}
