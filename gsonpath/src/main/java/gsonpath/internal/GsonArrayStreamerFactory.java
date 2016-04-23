package gsonpath.internal;

import gsonpath.GsonArrayStreamer;

/**
 * Created by Lachlan on 7/03/2016.
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

    public <T extends GsonArrayStreamer> T get(Class<T> type) {
        return (T) gsonArrayStreamerLoader.get(type);
    }
}
