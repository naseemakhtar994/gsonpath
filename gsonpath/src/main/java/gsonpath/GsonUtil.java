package gsonpath;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;

/**
 * Created by Lachlan on 7/03/2016.
 */
public class GsonUtil {

    public static boolean isValidValue(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.skipValue();
            return false;
        }
        return true;
    }

    public static String getStringSafely(JsonReader in) throws IOException {
        if (isValidValue(in)) {
            return in.nextString();
        }
        return null;
    }

    public static Boolean getBooleanSafely(JsonReader in) throws IOException {
        if (isValidValue(in)) {
            return in.nextBoolean();
        }
        return null;
    }

    public static Integer getIntegerSafely(JsonReader in) throws IOException {
        if (isValidValue(in)) {
            return in.nextInt();
        }
        return null;
    }

    public static Long getLongSafely(JsonReader in) throws IOException {
        if (isValidValue(in)) {
            return in.nextLong();
        }
        return null;
    }

    public static Double getDoubleSafely(JsonReader in) throws IOException {
        if (isValidValue(in)) {
            return in.nextDouble();
        }
        return null;
    }

}
