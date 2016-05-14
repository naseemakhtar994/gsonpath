package gsonpath;

import java.io.IOException;

public class JsonFieldMissingException extends IOException {
    public JsonFieldMissingException(String message) {
        super(message);
    }
}
