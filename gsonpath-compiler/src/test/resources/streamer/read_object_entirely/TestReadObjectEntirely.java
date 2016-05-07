package streamer.read_object_entirely;

import gsonpath.AutoGsonArrayStreamer;
import gsonpath.GsonArrayStreamer;

@AutoGsonArrayStreamer(rootField = "root")
public interface TestReadObjectEntirely extends GsonArrayStreamer<java.lang.String> {
}