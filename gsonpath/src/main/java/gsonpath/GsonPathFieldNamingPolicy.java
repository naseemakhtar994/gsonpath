package gsonpath;

/**
 * Provides the exact same functionality as the Gson FieldNamingPolicy enum, however this class was created to provide
 * an easy way to inherit a default value if required by using the {@link #IDENTITY_OR_INHERIT_DEFAULT_IF_AVAILABLE}
 * variation of the {@link #IDENTITY} value.
 */
public enum GsonPathFieldNamingPolicy {
    /**
     * Provides the same functionality as {@link #IDENTITY}, however this allows the processor
     * to know whether to use the default value or not.
     */
    IDENTITY_OR_INHERIT_DEFAULT_IF_AVAILABLE,

    /**
     * See the Gson documentation.
     */
    IDENTITY,

    /**
     * See the Gson documentation.
     */
    UPPER_CAMEL_CASE,

    /**
     * See the Gson documentation.
     */
    UPPER_CAMEL_CASE_WITH_SPACES,

    /**
     * See the Gson documentation.
     */
    LOWER_CASE_WITH_UNDERSCORES,

    /**
     * See the Gson documentation.
     */
    LOWER_CASE_WITH_DASHES
}
