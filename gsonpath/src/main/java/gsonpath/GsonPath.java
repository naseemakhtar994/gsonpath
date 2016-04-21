package gsonpath;

import gsonpath.internal.GsonPathTypeAdapterFactory;
import gsonpath.internal.GsonPathTypeArrayAdapterFactory;

/**
 * Created by Lachlan on 16/04/2016.
 */
public class GsonPath {

    public static GsonPathTypeAdapterFactory createTypeAdapterFactory() {
        return new GsonPathTypeAdapterFactory();
    }

    public static GsonPathTypeArrayAdapterFactory getArrayTypeAdapterFactory() {
        return new GsonPathTypeArrayAdapterFactory();
    }

    public static <T extends ArrayTypeAdapter> T getArrayTypeAdapter(Class<T> type) {
        return getArrayTypeAdapterFactory().get(type);
    }

}
