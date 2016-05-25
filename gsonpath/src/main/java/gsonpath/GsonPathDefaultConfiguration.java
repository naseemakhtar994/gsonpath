package gsonpath;

import com.google.gson.FieldNamingPolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a set of default values for the {@link gsonpath.AutoGsonAdapter} class.
 * <p/>
 * This can aid removing repetition for developers who are not happy with the defaults
 * provided out of the box.
 * <p/>
 * To use this class correctly, you must annotate a class, and then whenever you which to use
 * these defaults you must set the {@link gsonpath.AutoGsonAdapter#defaultConfiguration} value to point to
 * this annotated class.
 * <p/>
 * For example, creating defaults as follows:
 * <pre>
 * {@literal @}GsonPathDefaultConfiguration(flattenDelimiter = '$')
 * class GsonPathDefaultConfiguration
 * {
 * }
 * </pre>
 * and then using it later:
 * <pre>
 * {@literal @}AutoGsonAdapter(defaults = GsonPathDefaultConfiguration.class)
 * class ExampleModel
 * {
 * }
 * </pre>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GsonPathDefaultConfiguration {
    /**
     * Refer to the documentation here: {@link AutoGsonAdapter#ignoreNonAnnotatedFields}
     */
    boolean ignoreNonAnnotatedFields() default false;

    /**
     * Refer to the documentation here: {@link AutoGsonAdapter#flattenDelimiter}
     */
    char flattenDelimiter() default '.';

    /**
     * Refer to the documentation here: {@link AutoGsonAdapter#fieldNamingPolicy}
     */
    FieldNamingPolicy fieldNamingPolicy() default FieldNamingPolicy.IDENTITY;

    /**
     * Refer to the documentation here: {@link AutoGsonAdapter#serializeNulls}
     */
    boolean serializeNulls() default false;

    /**
     * Refer to the documentation here: {@link AutoGsonAdapter#fieldValidationType}
     */
    GsonFieldValidationType fieldValidationType() default GsonFieldValidationType.NO_VALIDATION;
}
