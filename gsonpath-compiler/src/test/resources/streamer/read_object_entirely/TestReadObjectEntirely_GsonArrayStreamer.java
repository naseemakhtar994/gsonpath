package streamer.read_object_entirely;

import static gsonpath.GsonUtil.*;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import gsonpath.AbstractGsonArrayStreamer;
import gsonpath.GsonArrayStreamer;

import java.io.IOException;
import java.lang.Override;
import java.lang.String;
import java.util.List;

public final class TestReadObjectEntirely_GsonArrayStreamer extends AbstractGsonArrayStreamer<String> implements TestReadObjectEntirely {
    @Override
    public String[] getArray(Gson gson, JsonReader in) throws JsonSyntaxException {
        String[] result = null;
        try {

            // Ensure the object is not null.
            if (!isValidValue(in)) {
                return null;
            }

            int jsonFieldCounter0 = 0;
            in.beginObject();

            while (in.hasNext()) {
                if (jsonFieldCounter0 == 1) {
                    in.skipValue();
                    continue;
                }

                switch (in.nextName()) {
                    case "root":
                        jsonFieldCounter0++;
                        result = gson.fromJson(in, String[].class);
                        break;

                    default:
                        in.skipValue();
                        break;
                }
            }

            in.endObject();
        } catch (IOException e) {
            throw new JsonSyntaxException(e);
        }
        return result;
    }

    @Override
    public List<String> getList(Gson gson, JsonReader in) throws JsonSyntaxException {
        List<String> result = null;
        try {

            // Ensure the object is not null.
            if (!isValidValue(in)) {
                return null;
            }

            int jsonFieldCounter0 = 0;
            in.beginObject();

            while (in.hasNext()) {
                if (jsonFieldCounter0 == 1) {
                    in.skipValue();
                    continue;
                }

                switch (in.nextName()) {
                    case "root":
                        jsonFieldCounter0++;
                        result = gson.fromJson(in, new com.google.gson.reflect.TypeToken<List<String>>() {
                        }.getType());
                        break;

                    default:
                        in.skipValue();
                        break;
                }
            }

            in.endObject();
        } catch (IOException e) {
            throw new JsonSyntaxException(e);
        }
        return result;
    }

    @Override
    public void streamArraySegmented(Gson gson, JsonReader in, int streamSize, GsonArrayStreamer.StreamCallback<String[]> callback) throws JsonSyntaxException {
        String[] results;
        StreamCallback.StreamHandler callbackResponse;
        int resultIndex;
        try {
            // Ensure the object is not null.
            if (!isValidValue(in)) {
                return;
            }

            results = new String[streamSize];
            callbackResponse = new StreamCallback.StreamHandler();
            resultIndex = -1;

            int jsonFieldCounter0 = 0;
            in.beginObject();

            while (in.hasNext()) {
                if (jsonFieldCounter0 == 1) {
                    in.skipValue();
                    continue;
                }

                switch (in.nextName()) {
                    case "root":
                        jsonFieldCounter0++;
                        in.beginArray();
                        while (in.hasNext()) {
                            results[++resultIndex] = gson.fromJson(in, String.class);

                            // Once we reach the requested stream size, we should return the results.
                            if (resultIndex == streamSize - 1) {
                                callback.onValueParsed(results, streamSize, callbackResponse);

                                if (callbackResponse.isStreamStopped()) {
                                    // Since we stop the stream mid-way, we cannot call end-array safely.
                                    return;
                                }

                                resultIndex = -1;
                            }
                        }
                        in.endArray();

                    default:
                        in.skipValue();
                        break;
                }
            }

            in.endObject();
        } catch (IOException e) {
            throw new JsonSyntaxException(e);
        }

        // We have left over results to send back.
        if (resultIndex >= 0) {
            // To avoid creating a new array, we will simply remove the invalid results at the end.
            for (int i = resultIndex + 1; i < streamSize; i++) {
                results[i] = null;
            }

            callback.onValueParsed(results, resultIndex + 1, callbackResponse);
        }
    }
}