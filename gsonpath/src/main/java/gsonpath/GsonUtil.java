package gsonpath;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;

/**
 * A set of Gson utilities which expose functionality to read content from a JsonReader
 * safely without throwing IOExceptions.
 */
public class GsonUtil {

    /**
     * Determines whether the next value within the reader is not null.
     */
    public static boolean isValidValue(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.skipValue();
            return false;
        }
        return true;
    }

    /**
     * Attempts to obtain the next string value from the reader if possible, otherwise
     * it will return null.
     */
    @SuppressWarnings("unused")
    public static String getStringSafely(JsonReader in) throws IOException {
        if (isValidValue(in)) {
            return in.nextString();
        }
        return null;
    }

    /**
     * Attempts to obtain the next boolean value from the reader if possible, otherwise
     * it will return null.
     */
    @SuppressWarnings("unused")
    public static Boolean getBooleanSafely(JsonReader in) throws IOException {
        if (isValidValue(in)) {
            return in.nextBoolean();
        }
        return null;
    }

    /**
     * Attempts to obtain the next integer value from the reader if possible, otherwise
     * it will return null.
     */
    @SuppressWarnings("unused")
    public static Integer getIntegerSafely(JsonReader in) throws IOException {
        if (isValidValue(in)) {
            return in.nextInt();
        }
        return null;
    }

    /**
     * Attempts to obtain the next long value from the reader if possible, otherwise
     * it will return null.
     */
    @SuppressWarnings("unused")
    public static Long getLongSafely(JsonReader in) throws IOException {
        if (isValidValue(in)) {
            return in.nextLong();
        }
        return null;
    }

    /**
     * Attempts to obtain the next double value from the reader if possible, otherwise
     * it will return null.
     */
    @SuppressWarnings("unused")
    public static Double getDoubleSafely(JsonReader in) throws IOException {
        if (isValidValue(in)) {
            return in.nextDouble();
        }
        return null;
    }

}
