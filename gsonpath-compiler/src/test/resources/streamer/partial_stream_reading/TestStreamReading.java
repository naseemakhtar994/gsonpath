package streamer.partial_stream_reading;

import gsonpath.AutoGsonArrayStreamer;
import gsonpath.GsonArrayStreamer;

@AutoGsonArrayStreamer(rootField = "root", consumeReaderFully = false)
public interface TestStreamReading extends GsonArrayStreamer<String> {
}