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
public interface GsonArrayStreamer<T> {

    /**
     * Obtains an array of objects from a Json array which exists within the reader.
     *
     * @param gson standard Gson main class. Must have all the required type adapters defined.
     * @param in   the reader used to create the {@link JsonReader} which will read the json array.
     * @return an array of results if reading the Json was a success, or null if the required field was not found.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    T[] getArray(Gson gson, Reader in) throws JsonSyntaxException;

    /**
     * Obtains an array of objects from a Json array which exists within the reader.
     *
     * @param gson standard Gson main class. Must have all the required type adapters defined.
     * @param in   the reader which will read the json array
     * @return an array of results if reading the Json was a success, or null if the required field was not found.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    T[] getArray(Gson gson, JsonReader in) throws JsonSyntaxException;

    /**
     * Obtains a list of objects from a Json array which exists within the reader.
     *
     * @param gson standard Gson main class. Must have all the required type adapters defined.
     * @param in   the reader used to create the {@link JsonReader} which will read the json array.
     * @return a list of results if reading the Json was a success, or null if the required field was not found.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    List<T> getList(Gson gson, Reader in) throws JsonSyntaxException;

    /**
     * Obtains a list of objects from a Json array which exists within the reader.
     *
     * @param gson standard Gson main class. Must have all the required type adapters defined.
     * @param in   the reader which will read the json array
     * @return a list of results if reading the Json was a success, or null if the required field was not found.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    List<T> getList(Gson gson, JsonReader in) throws JsonSyntaxException;

    /**
     * Streams a list of objects from a Json array which exists within the reader. Each object is returned
     * one at a time using the callback.
     * <p/>
     * The streaming can be cancelled at anytime using the {@link StreamCallback.StreamHandler} object
     * within the callback.
     *
     * @param gson     standard Gson main class. Must have all the required type adapters defined.
     * @param in       the reader used to create the {@link JsonReader} which will read the Json array.
     * @param callback a callback which returns each individual object within the Json array.
     *                 The callback exposes a mechanism to stop streaming the Json array using the
     *                 {@link StreamCallback.StreamHandler} class.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    void streamArray(Gson gson, Reader in, StreamCallback<T> callback) throws JsonSyntaxException;


    /**
     * Streams a list of objects from a Json array which exists within the reader. Each object is returned
     * one at a time using the callback.
     * <p/>
     * The streaming can be cancelled at anytime using the {@link StreamCallback.StreamHandler} object
     * within the callback.
     *
     * @param gson     standard Gson main class. Must have all the required type adapters defined.
     * @param in       the reader which will read the json array
     * @param callback a callback which returns each individual object within the Json array.
     *                 The callback exposes a mechanism to stop streaming the Json array using the
     *                 {@link StreamCallback.StreamHandler} class.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    void streamArray(Gson gson, JsonReader in, StreamCallback<T> callback) throws JsonSyntaxException;

    /**
     * Streams a list of objects from a Json array which exists within the reader. An array of objects, of size
     * 'segmentSize' is returned each time the array has been filled.
     * <p/>
     * The streaming can be cancelled at anytime using the {@link StreamCallback.StreamHandler} object
     * within the callback.
     *
     * @param gson     standard Gson main class. Must have all the required type adapters defined.
     * @param in       the reader used to create the {@link JsonReader} which will read the Json array.
     * @param callback a callback which returns each individual object within the Json array.
     *                 The callback exposes a mechanism to stop streaming the Json array using the
     *                 {@link StreamCallback.StreamHandler} class.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    void streamArraySegmented(Gson gson, Reader in, int segmentSize, StreamCallback<T[]> callback) throws JsonSyntaxException;

    /**
     * Streams a list of objects from a Json array which exists within the reader. An array of objects, of size
     * 'segmentSize' is returned each time the array has been filled.
     * <p/>
     * The streaming can be cancelled at anytime using the {@link StreamCallback.StreamHandler} object
     * within the callback.
     *
     * @param gson     standard Gson main class. Must have all the required type adapters defined.
     * @param in       the reader which will read the json array
     * @param callback a callback which returns each individual object within the Json array.
     *                 The callback exposes a mechanism to stop streaming the Json array using the
     *                 {@link StreamCallback.StreamHandler} class.
     * @throws JsonSyntaxException thrown by Gson. Ensure you close your input stream as this method will not close it.
     */
    void streamArraySegmented(Gson gson, JsonReader in, int segmentSize, StreamCallback<T[]> callback) throws JsonSyntaxException;

    /**
     * A callback used when streaming json array content as opposed to reading the entire array into memory.
     * <p/>
     * Using the {@link StreamHandler} it is possible to cancel reading the stream midway.
     */
    public interface StreamCallback<T> {
        void onValueParsed(T value, int length, StreamHandler response);

        /**
         * A handler exposed through the {@link StreamCallback} which allows stopping a stream midway.
         */
        public static class StreamHandler {
            private boolean streamStopped = false;

            /**
             * Determines whether the stream should be stopped.
             */
            public boolean isStreamStopped() {
                return streamStopped;
            }

            /**
             * Informs the streamer to stop reading the content immediately.
             */
            public void stopStream() {
                streamStopped = true;
            }
        }
    }

}
