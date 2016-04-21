package gsonpath.internal;

import gsonpath.ArrayTypeAdapter;

/**
 * Created by Lachlan on 7/03/2016.
 */
public class GsonPathTypeArrayAdapterFactory {
    private TypeAdapterArrayLoader typeAdapterArrayLoader;

    public GsonPathTypeArrayAdapterFactory() {
        try {
            typeAdapterArrayLoader = (TypeAdapterArrayLoader) Class.forName("gsonpath.GeneratedTypeAdapterArrayLoader").newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to find generated TypeAdapterArrayLoader");
        }
    }

    public <T extends ArrayTypeAdapter> T get(Class<T> type) {
        return (T) typeAdapterArrayLoader.get(type);
    }
}
