package gsonpath;

public class JsonFieldMissingException extends RuntimeException {
    public JsonFieldMissingException(String message) {
        super(message);
    }
}
