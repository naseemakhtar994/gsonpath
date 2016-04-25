package gsonpath;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.Reader;
import java.util.List;

/**
 * A partial implementation of the {@link gsonpath.GsonArrayStreamer} which reduces the
 * complexity for code generation since most of the methods can call their overloaded variants.
 * <p/>
 * All generated {@link GsonArrayStreamer} classes will extend this class, which is why
 * the {@link gsonpath.AutoGsonArrayStreamer} annotation can only be used on an interface.
 */
public abstract class AbstractGsonArrayStreamer<T> implements GsonArrayStreamer<T> {
    @Override
    public T[] getArray(Gson gson, Reader in) throws JsonSyntaxException {
        return getArray(gson, gson.newJsonReader(in));
    }

    @Override
    public List<T> getList(Gson gson, Reader in) throws JsonSyntaxException {
        return getList(gson, gson.newJsonReader(in));
    }

    @Override
    public void streamArray(Gson gson, Reader in, final StreamCallback<T> callback) throws JsonSyntaxException {
        streamArraySegmented(gson, gson.newJsonReader(in), 1, new StreamCallback<T[]>() {
            @Override
            public void onValueParsed(T[] value, int length, StreamHandler response) {
                callback.onValueParsed(value[0], length, response);
            }
        });
    }

    @Override
    public void streamArray(Gson gson, JsonReader in, final StreamCallback<T> callback) throws JsonSyntaxException {
        streamArraySegmented(gson, in, 1, new StreamCallback<T[]>() {
            @Override
            public void onValueParsed(T[] value, int length, StreamHandler response) {
                callback.onValueParsed(value[0], length, response);
            }
        });
    }

    @Override
    public void streamArraySegmented(Gson gson, Reader in, int segmentSize, StreamCallback<T[]> callback) throws JsonSyntaxException {
        streamArraySegmented(gson, gson.newJsonReader(in), segmentSize, callback);
    }
}
