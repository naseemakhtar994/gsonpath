package gsonpath;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.Reader;
import java.util.List;

/**
 * Exposes functionality to obtain an array directly from a json file without
 * declaring a container class, or extending a collection directly.
 */
public interface ArrayTypeAdapter<T> {

    /**
     * Obtains an array of objects from a Json array which exists within the reader.
     *
     * @param gson   standard Gson main class. Must have all the required type adapters defined.
     * @param reader the reader used to create the {@link JsonReader} which will read the json array.
     * @return an array of results if reading the Json was a success, or null if the required field was not found.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    T[] getArray(Gson gson, Reader reader) throws JsonSyntaxException;

    /**
     * Obtains an array of objects from a Json array which exists within the reader.
     *
     * @param gson       standard Gson main class. Must have all the required type adapters defined.
     * @param jsonReader the reader which will read the json array
     * @return an array of results if reading the Json was a success, or null if the required field was not found.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    T[] getArray(Gson gson, JsonReader jsonReader) throws JsonSyntaxException;

    /**
     * Obtains a list of objects from a Json array which exists within the reader.
     *
     * @param gson   standard Gson main class. Must have all the required type adapters defined.
     * @param reader the reader used to create the {@link JsonReader} which will read the json array.
     * @return a list of results if reading the Json was a success, or null if the required field was not found.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    List<T> getList(Gson gson, Reader reader) throws JsonSyntaxException;

    /**
     * Obtains a list of objects from a Json array which exists within the reader.
     *
     * @param gson       standard Gson main class. Must have all the required type adapters defined.
     * @param jsonReader the reader which will read the json array
     * @return a list of results if reading the Json was a success, or null if the required field was not found.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    List<T> getList(Gson gson, JsonReader jsonReader) throws JsonSyntaxException;

    /**
     * Streams a list of objects from a Json array which exists within the reader. Each object is returned
     * one at a time using the callback.
     * <p/>
     * The streaming can be cancelled at anytime using the {@link gsonpath.StreamCallback.StreamHandler} object
     * within the callback.
     *
     * @param gson     standard Gson main class. Must have all the required type adapters defined.
     * @param reader   the reader used to create the {@link JsonReader} which will read the Json array.
     * @param callback a callback which returns each individual object within the Json array.
     *                 The callback exposes a mechanism to stop streaming the Json array using the
     *                 {@link gsonpath.StreamCallback.StreamHandler} class.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    void streamArray(Gson gson, Reader reader, StreamCallback<T> callback) throws JsonSyntaxException;


    /**
     * Streams a list of objects from a Json array which exists within the reader. Each object is returned
     * one at a time using the callback.
     * <p/>
     * The streaming can be cancelled at anytime using the {@link gsonpath.StreamCallback.StreamHandler} object
     * within the callback.
     *
     * @param gson       standard Gson main class. Must have all the required type adapters defined.
     * @param jsonReader the reader which will read the json array
     * @param callback   a callback which returns each individual object within the Json array.
     *                   The callback exposes a mechanism to stop streaming the Json array using the
     *                   {@link gsonpath.StreamCallback.StreamHandler} class.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    void streamArray(Gson gson, JsonReader jsonReader, StreamCallback<T> callback) throws JsonSyntaxException;

    /**
     * Streams a list of objects from a Json array which exists within the reader. An array of objects, of size
     * 'segmentSize' is returned each time the array has been filled.
     * <p/>
     * The streaming can be cancelled at anytime using the {@link gsonpath.StreamCallback.StreamHandler} object
     * within the callback.
     *
     * @param gson     standard Gson main class. Must have all the required type adapters defined.
     * @param reader   the reader used to create the {@link JsonReader} which will read the Json array.
     * @param callback a callback which returns each individual object within the Json array.
     *                 The callback exposes a mechanism to stop streaming the Json array using the
     *                 {@link gsonpath.StreamCallback.StreamHandler} class.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    void streamArraySegmented(Gson gson, Reader reader, int segmentSize, StreamCallback<T[]> callback) throws JsonSyntaxException;

    /**
     * Streams a list of objects from a Json array which exists within the reader. An array of objects, of size
     * 'segmentSize' is returned each time the array has been filled.
     * <p/>
     * The streaming can be cancelled at anytime using the {@link gsonpath.StreamCallback.StreamHandler} object
     * within the callback.
     *
     * @param gson       standard Gson main class. Must have all the required type adapters defined.
     * @param jsonReader the reader which will read the json array
     * @param callback   a callback which returns each individual object within the Json array.
     *                   The callback exposes a mechanism to stop streaming the Json array using the
     *                   {@link gsonpath.StreamCallback.StreamHandler} class.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    void streamArraySegmented(Gson gson, JsonReader jsonReader, int segmentSize, StreamCallback<T[]> callback) throws JsonSyntaxException;

}
