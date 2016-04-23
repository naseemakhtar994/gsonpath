package gsonpath;

import gsonpath.internal.GsonArrayStreamerFactory;
import gsonpath.internal.GsonPathTypeAdapterFactory;

/**
 * Created by Lachlan on 16/04/2016.
 */
public class GsonPath {

    public static GsonPathTypeAdapterFactory createTypeAdapterFactory() {
        return new GsonPathTypeAdapterFactory();
    }

    public static GsonArrayStreamerFactory getGsonArrayStreamerFactory() {
        return new GsonArrayStreamerFactory();
    }

    public static <T extends GsonArrayStreamer> T getArrayStreamer(Class<T> type) {
        return getGsonArrayStreamerFactory().get(type);
    }

}
