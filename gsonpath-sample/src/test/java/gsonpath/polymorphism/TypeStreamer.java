package gsonpath.polymorphism;

import gsonpath.AutoGsonArrayStreamer;
import gsonpath.GsonArrayStreamer;

/**
 * Created by Lachlan on 25/04/2016.
 */
@AutoGsonArrayStreamer(rootField = "items")
public interface TypeStreamer extends GsonArrayStreamer<Type> {
}
