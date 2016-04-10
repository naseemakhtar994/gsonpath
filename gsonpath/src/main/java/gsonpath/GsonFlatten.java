package gsonpath;

import java.lang.annotation.*;

/**
 * An annotation that indicates that the given JSON object found for a given GSON
 * SerializedName should be flattened into a String.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Inherited
public @interface GsonFlatten {
}
