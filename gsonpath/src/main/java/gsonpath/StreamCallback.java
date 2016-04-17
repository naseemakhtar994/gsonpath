package gsonpath;

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
