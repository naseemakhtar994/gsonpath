package gsonpath.internal;

import gsonpath.GsonArrayStreamer;

/**
 * An interface which is used by the generated 'GeneratedGsonArrayStreamerLoader' class
 * and provides a mechanism to load {@link gsonpath.GsonArrayStreamer} classes
 * based on a particular class type.
 */
public interface GsonArrayStreamerLoader {

    /**
     * Obtains a concrete implementation of a given class which implements the {@link GsonArrayStreamer}
     * interface.
     * <p/>
     * The concrete implementation is generated at compile time when the {@link gsonpath.AutoGsonArrayStreamer}
     * annotation is used correctly. For correct usage, check the documentation of the annotation.
     *
     * @param type the interface class which expects a concrete implementation returned.
     * @param <T>  the class type which must implement {@link GsonArrayStreamer}
     * @return the concrete implementation of the interface (if it is found)
     */
    <T extends GsonArrayStreamer> GsonArrayStreamer get(Class<T> type);
}
