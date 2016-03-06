package gsonpath;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;

/**
 * Created by Lachlan on 7/03/2016.
 */
public class GsonPathUtil {

    public static String getStringSafely(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.skipValue();
            return null;
        }
        return in.nextString();
    }

}
