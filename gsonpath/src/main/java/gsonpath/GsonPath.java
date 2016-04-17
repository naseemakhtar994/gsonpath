package gsonpath;

import gsonpath.internal.GsonPathTypeAdapterFactory;

/**
 * Created by Lachlan on 16/04/2016.
 */
public class GsonPath {

    public static GsonPathTypeAdapterFactory createTypeAdapterFactory() {
        return new GsonPathTypeAdapterFactory();
    }

}
