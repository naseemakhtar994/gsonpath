package gsonpath;

/**
 * Defines a delimiter character used by the Gson Path library when dealing with nested fields.
 * <p/>
 * This exists as another annotation as opposed to using a char directly since it provides an easy mechanism
 * to determine whether the Type Adapter should use the default delimiter if supplied, or override it.
 */
public @interface FlattenDelimiter {
    /**
     * The delimiter value
     */
    char value();

    /**
     * Whether this delimiter should be overridden by the default value.
     * <p/>
     * Typically you should leave this as false.
     */
    boolean inheritDefaultIfAvailable() default false;
}
