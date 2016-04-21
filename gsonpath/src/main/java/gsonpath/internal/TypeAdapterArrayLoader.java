package gsonpath.internal;

import gsonpath.ArrayTypeAdapter;

/**
 * Created by Lachlan on 7/03/2016.
 */
public interface TypeAdapterArrayLoader {
    <T extends ArrayTypeAdapter> ArrayTypeAdapter get(Class<T> type);
}
