package gsonpath;

import com.google.gson.FieldNamingPolicy;

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
 * (such as {@link FieldNamingPolicy}) must be specified within the annotation itself.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface AutoGsonAdapter {
    boolean ignoreNonAnnotatedFields() default false;

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
     */
    char flattenDelimiter() default '.';

    /**
     * Exposes the Gson field naming policy at compile time rather than runtime.
     * <p/>
     * Note: This will affect every version of this class regardless of how the
     * gson object is constructed.
     */
    FieldNamingPolicy fieldNamingPolicy() default FieldNamingPolicy.IDENTITY;

    boolean serializeNulls() default false;
}
