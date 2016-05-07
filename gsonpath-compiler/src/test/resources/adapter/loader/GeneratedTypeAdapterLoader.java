package gsonpath;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import gsonpath.internal.TypeAdapterLoader;

import java.lang.Override;

public final class GeneratedTypeAdapterLoader implements TypeAdapterLoader {
    @Override
    public TypeAdapter create(Gson gson, TypeToken type) {
        Class rawType = type.getRawType();
        if (rawType.equals(adapter.loader.TestLoaderSource1.class)) {
            return new adapter.loader.TestLoaderSource1_GsonTypeAdapter(gson);

        } else if (rawType.equals(adapter.loader.TestLoaderSource2.class)) {
            return new adapter.loader.TestLoaderSource2_GsonTypeAdapter(gson);
        }

        return null;
    }
}