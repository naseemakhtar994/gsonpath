package gsonpath.internal;

import gsonpath.GsonArrayStreamer;

/**
 * Created by Lachlan on 7/03/2016.
 */
public interface GsonArrayStreamerLoader {
    <T extends GsonArrayStreamer> GsonArrayStreamer get(Class<T> type);
}
