package gsonpath;

import gsonpath.internal.GsonArrayStreamerFactory;
import gsonpath.internal.GsonPathTypeAdapterFactory;

/**
 * The primary class to use when using the GsonPath library.
 * <p/>
 * It supplies factories which expose auto generated class created using the
 * {@link gsonpath.AutoGsonAdapter} and {@link gsonpath.AutoGsonArrayStreamer} annotations.
 */
public class GsonPath {

    /**
     * Creates an instance of the {@link gsonpath.internal.GsonPathTypeAdapterFactory} class.
     * <p/>
     * This factory is used to map the auto generated {@link com.google.gson.TypeAdapter} classes created using the
     * {@link gsonpath.AutoGsonAdapter} annotation.
     * <p/>
     * Only a single use of reflection is used within the constructor, so it isn't critical to hold onto this reference
     * for later usage.
     */
    public static GsonPathTypeAdapterFactory createTypeAdapterFactory() {
        return new GsonPathTypeAdapterFactory();
    }

    /**
     * Creates an instance of the {@link gsonpath.internal.GsonArrayStreamerFactory} class.
     * <p/>
     * This factory is used to map the auto generated {@link gsonpath.GsonArrayStreamer} classes created using the
     * {@link gsonpath.AutoGsonArrayStreamer} annotation.
     * <p/>
     * Only a single use of reflection is used within the constructor, so it isn't critical to hold onto this reference
     * for later usage.
     */
    public static GsonArrayStreamerFactory getGsonArrayStreamerFactory() {
        return new GsonArrayStreamerFactory();
    }

    /**
     * Creates an instance of the {@link gsonpath.internal.GsonArrayStreamerFactory} class and
     * returns a generated implementation of a {@link gsonpath.GsonArrayStreamer} for a given class
     * type which <b>must</b> implement {@link gsonpath.GsonArrayStreamer}.
     * <p/>
     * See the {@link #getGsonArrayStreamerFactory} documentation for further details.
     */
    public static <T extends GsonArrayStreamer> T getArrayStreamer(Class<T> type) {
        return getGsonArrayStreamerFactory().get(type);
    }

}
