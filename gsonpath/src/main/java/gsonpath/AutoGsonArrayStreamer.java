package gsonpath;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class annotated with this annotation will automatically generate a
 * Gson {@link gsonpath.GsonArrayStreamer} at compile time.
 * <p/>
 * The generated class is useful when wanting to read a JSON array directly
 * from an input stream without creating a wrapper class which exists only
 * to allow a Gson {@link com.google.gson.TypeAdapter} to read it properly.
 * <p/>
 * It also has more advanced features such as allowing streaming the array elements
 * one-by-one, or in a segmented fashion.
 * <p/>
 * To use this annotation correctly without issues, the annotated class must be an interface
 * which extends the {@link gsonpath.GsonArrayStreamer} interface. If these preconditions
 * are not met, a compiler error will be thrown.
 * <p/>
 * For more information, see the {@link GsonArrayStreamer} documentation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface AutoGsonArrayStreamer {
    /**
     * By leaving value blank, it is implied that the root of the json is an array
     * instead of an object.
     * <p/>
     * For more information as to how this property works, see the {@link gsonpath.AutoGsonAdapter}
     * documentation.
     */
    String rootField() default "";

    /**
     * Typically Gson validates whether a stream has been read in its entirety, and throws
     * an exception if it has not.
     * <p/>
     * When using the 'rootField' property, we can yield a performance gain by ignoring the rest
     * of the stream as soon as the specified Json Array has been read.
     * <p/>
     * The downsides to this is that the stream is now in an invalid state. By default we want
     * to ensure that the stream is read correctly to maintain good compatibility with Gson.
     * However if you use the streamer directly on the input stream, you can deliberately disable
     * this functionality and reap some level of performance gains.
     */
    boolean consumeReaderFully() default true;
}
