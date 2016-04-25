package gsonpath.internal;

import gsonpath.GsonArrayStreamer;

/**
 * A factory which exposes auto generated {@link gsonpath.GsonArrayStreamer} classes which
 * are created using the {@link gsonpath.AutoGsonArrayStreamer} annotation.
 * <p/>
 * The creation of the {@link gsonpath.GsonArrayStreamer} classes is delegated to
 * another generated class (of type {@link GsonArrayStreamerLoader}) which can create the correct
 * {@link GsonArrayStreamer} classes without using any reflection.
 * <p/>
 * The factory only uses reflection once, when it needs to obtain this delegated {@link GsonArrayStreamerLoader}
 * class.
 */
public class GsonArrayStreamerFactory {
    private GsonArrayStreamerLoader gsonArrayStreamerLoader;

    public GsonArrayStreamerFactory() {
        try {
            gsonArrayStreamerLoader = (GsonArrayStreamerLoader) Class.forName("gsonpath.GeneratedGsonArrayStreamerLoader").newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to find generated TypeAdapterArrayLoader");
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends GsonArrayStreamer> T get(Class<T> type) {
        return (T) gsonArrayStreamerLoader.get(type);
    }
}
