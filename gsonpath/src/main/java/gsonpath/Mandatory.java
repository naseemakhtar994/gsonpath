package gsonpath;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Inherited
public @interface Mandatory {
}
