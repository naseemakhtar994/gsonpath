package gsonpath;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.Reader;
import java.util.List;

/**
 * Created by Lachlan on 18/04/2016.
 */
public abstract class AbstractArrayTypeAdapter<T> implements ArrayTypeAdapter<T> {
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
