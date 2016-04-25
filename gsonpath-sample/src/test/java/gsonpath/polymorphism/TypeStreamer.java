package gsonpath.polymorphism;

import gsonpath.AutoGsonArrayStreamer;
import gsonpath.GsonArrayStreamer;

@AutoGsonArrayStreamer(rootField = "items")
public interface TypeStreamer extends GsonArrayStreamer<Type> {
}
