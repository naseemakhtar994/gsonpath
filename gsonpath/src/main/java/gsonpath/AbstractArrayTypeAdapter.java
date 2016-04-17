package gsonpath;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.Reader;
import java.util.List;

/**
 * Created by Lachlan on 18/04/2016.
 */
public abstract class AbstractArrayTypeAdapter<T> implements ArrayTypeAdapter<T> {
    @Override
    public T[] getArray(Gson gson, Reader reader) throws JsonSyntaxException {
        return getArray(gson, gson.newJsonReader(reader));
    }

    @Override
    public List<T> getList(Gson gson, Reader reader) throws JsonSyntaxException {
        return getList(gson, gson.newJsonReader(reader));
    }

    @Override
    public void streamArray(Gson gson, Reader reader, StreamCallback<T> callback) throws JsonSyntaxException {
        streamArray(gson, gson.newJsonReader(reader), callback);
    }

    @Override
    public void streamArraySegmented(Gson gson, Reader reader, int segmentSize, StreamCallback<T[]> callback) throws JsonSyntaxException {
        streamArraySegmented(gson, gson.newJsonReader(reader), segmentSize, callback);
    }
}
