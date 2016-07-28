package gsonpath;

/**
 * Provides a text substitution mechanism to the {@link com.google.gson.annotations.SerializedName} annotation
 * when used in conjunction with the {@link AutoGsonAdapter}.
 * <p>
 * A string replacement is performed by looking for the original text wrapped wih curly braces (e.g. for a original
 * term of 'ORIGINAL', the string replacement actually looks for '{ORIGINAL}', and replaces it with the replacement
 * text. This is very useful for when used with inheritance, as you can define a base class with substitution values,
 * and then specify a different replacement values within each concrete class.
 *
 * @author Lachlan McKee
 */
public @interface PathSubstitution {
    /**
     * The original text which will be replaced
     * <p>
     * The text replacement looks for the text with surrounding curly braces.
     * e.g. for a original term of 'ORIGINAL', the string replacement actually looks for '{ORIGINAL}'
     */
    String original();

    /**
     * The text used to override the original text.
     */
    String replacement();
}
