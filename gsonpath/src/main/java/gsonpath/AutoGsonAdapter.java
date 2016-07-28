package gsonpath;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class annotated with this annotation will automatically generate a Gson
 * {@link com.google.gson.TypeAdapter} at compile time.
 * <p/>
 * The benefit of this annotation is that it removes the majority of reflection used by
 * Gson, and also avoids problems caused by obfuscators such as proguard since the field names
 * are not obtained at runtime, instead they are statically referenced ahead of time.
 * <p/>
 * Another benefit of this generated {@link com.google.gson.TypeAdapter} class is that the
 * {@link com.google.gson.annotations.SerializedName} gson annotation is able to use very basic
 * JsonPath style notation. It can specify a tree branch notation which allows the POJO to
 * be much flatter than standard implementations
 * <p/>
 * For example, for a given JSON file:
 * <pre>
 * {
 *      "value1": 1
 *      "left": {
 *          "path": {
 *              "value2": 1
 *          }
 *      }
 * }
 * </pre>
 * And a class annotated as follows:
 * <pre>
 *
 * {@literal @}AutoGsonAdapter
 * class LeftPath
 * {
 *      int value1;
 *      {@literal @}SerializedName("left.path.value2") // or {@literal @}SerializedName("left.path.")
 *      int value2;
 * }
 * </pre>
 * The generated {@link com.google.gson.TypeAdapter} will map this nested JSON into a single POJO without any boilerplate code
 * being written.
 * <p/>
 * Note: As a consequence of generating code at compile time, some flexibility is lost surrounding
 * {@link com.google.gson.Gson} configurations. Therefore any Gson specific configurations
 * (such as {@link GsonPathFieldNamingPolicy}) must be specified within the annotation itself.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface AutoGsonAdapter {
    /**
     * Specifies a set of defaults which can be used to set a different set of defaults as opposed
     * to the ones provided out of the box.
     * <p/>
     * For further information about how to use this, see {@link GsonPathDefaultConfiguration}
     */
    Class<?> defaultConfiguration() default void.class;

    /**
     * Determines whether fields that do not use the 'SerializedName' gson annotation are added to the Type Adapter
     * or not. By default all fields are added.
     * <p/>
     * To exclude fields on a case-by-case basis, see {@link gsonpath.ExcludeField}
     */
    InheritableBoolean ignoreNonAnnotatedFields() default InheritableBoolean.FALSE_OR_INHERIT_DEFAULT_IF_AVAILABLE;

    /**
     * Specifies the root field where the generated {@link com.google.gson.TypeAdapter} will begin
     * to access the declared fields.
     * <p/>
     * If left blank, nothing will change, and the fields will be read immediately from the reader.
     * <p/>
     * However, if the root field is specified, then the reader will drill down into a specific
     * tree branch of the JSON and then commence reading the exposed fields.
     * <p/>
     * E.g. For a given JSON file:
     * <pre>
     * {
     *      "left": {
     *          "path": {
     *              "value1": 1
     *          }
     *          "ignored": "unused"
     *      },
     *      "right" {
     *          "ignored": "unused"
     *      }
     * }
     * </pre>
     * And a class annotated as follows:
     * <pre>
     *
     * {@literal @}AutoGsonAdapter(rootField = "left.path")
     * class LeftPath
     * {
     *      int value1;
     * }
     * </pre>
     * Then the reader will read into the 'left' and 'path' section of the JSON and
     * then read the 'value1' property as usual.
     */
    String rootField() default "";

    /**
     * The delimiter used to flatten Json nested structures into a single POJO.
     * <p/>
     * By default this is set to using the '.' character. If required you can override
     * this to using a different character, and all the fields within this class will
     * use this delimiter instead.
     * <p/>
     * Value will inherit from a 'defaults' class which uses the {@link GsonPathDefaultConfiguration}
     * annotation. If you don't wish to inherit, specify an explicit delimiter.
     */
    FlattenDelimiter flattenDelimiter() default @FlattenDelimiter(value = '.', inheritDefaultIfAvailable = true);

    /**
     * Exposes the Gson field naming policy at compile time rather than runtime.
     * <p/>
     * Note: This will affect every version of this class regardless of how the
     * gson object is constructed.
     * <p/>
     * Value will inherit from a 'defaults' class which uses the {@link GsonPathDefaultConfiguration}
     * annotation. If you don't wish to inherit, specify an explicit fieldNamingPolicy.
     */
    GsonPathFieldNamingPolicy fieldNamingPolicy() default GsonPathFieldNamingPolicy.IDENTITY_OR_INHERIT_DEFAULT_IF_AVAILABLE;

    /**
     * Determines whether the Type Adapter will allow null values into the JSON when writing.
     * <p/>
     * Refer to the Gson documentation for further details.
     */
    InheritableBoolean serializeNulls() default InheritableBoolean.FALSE_OR_INHERIT_DEFAULT_IF_AVAILABLE;

    /**
     * Defines validation rule which can be used to ensure that the parsed JSON conforms to a particular format.
     * <p/>
     * If the validation is enabled and a JSON document does not conform, an exception will be thrown, and the
     * parser will stop immediately.
     * <p/>
     * The validation is relies on the 'NonNull' (also 'Nonnull', 'NotNull' and 'Notnull') and 'Nullable' annotations.
     * <p/>
     * The feature allows you to create a contract where you can be sure that the POJO has all the data that you expect.
     */
    GsonFieldValidationType fieldValidationType() default GsonFieldValidationType.NO_VALIDATION_OR_INHERIT_DEFAULT_IF_AVAILABLE;

    /**
     * An array of substitutions which are applied to all fields annotated with {@link com.google.gson.annotations.SerializedName}.
     * <p>
     * This also includes fields which are inherited. It provides a useful mechanism to reuse the same model for
     * json with different names, but a similar structure.
     * <p>
     * See the {@link PathSubstitution} Javadoc for more details.
     */
    PathSubstitution[] substitutions() default {};
}
