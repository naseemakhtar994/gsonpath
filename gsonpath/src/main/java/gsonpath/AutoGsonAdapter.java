package gsonpath;

import com.google.gson.FieldNamingPolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Lachlan on 1/03/2016.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface AutoGsonAdapter {
    boolean ignoreNonAnnotatedFields() default false;

    String rootField() default "";

    /**
     * Exposes the Gson field naming policy at compile time rather than runtime.
     * <p/>
     * Note: This will affect every version of this class regardless of how the
     * gson object is constructed.
     */
    FieldNamingPolicy fieldNamingPolicy() default FieldNamingPolicy.IDENTITY;
}
