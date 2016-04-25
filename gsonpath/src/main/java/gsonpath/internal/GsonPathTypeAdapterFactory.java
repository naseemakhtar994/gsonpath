package gsonpath.internal;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * An implementation of {@link TypeAdapterFactory} which exposes the auto generated
 * {@link com.google.gson.TypeAdapter} classes which are created using the
 * {@link gsonpath.AutoGsonAdapter} annotation.
 * <p/>
 * The creation of the {@link com.google.gson.TypeAdapter} classes is delegated to
 * another generated class (of type {@link TypeAdapterLoader}) which can create the correct
 * {@link com.google.gson.TypeAdapter} classes without using any reflection.
 * <p/>
 * The factory only uses reflection once, when it needs to obtain this delegated {@link TypeAdapterLoader}
 * class.
 */
public class GsonPathTypeAdapterFactory implements TypeAdapterFactory {
    private TypeAdapterLoader typeAdapterLoader;

    public GsonPathTypeAdapterFactory() {
        try {
            typeAdapterLoader = (TypeAdapterLoader) Class.forName("gsonpath.GeneratedTypeAdapterLoader").newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to find generated TypeAdapterArrayLoader");
        }
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return typeAdapterLoader.create(gson, type);
    }
}
