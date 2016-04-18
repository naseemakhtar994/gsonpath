package gsonpath;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Lachlan on 1/03/2016.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface AutoGsonArrayAdapter {
    /**
     * By leaving value blank, it is implied that the root
     * of the json is an array instead of an object.
     */
    String rootField() default "";
}
