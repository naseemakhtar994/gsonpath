package gsonpath.generator;

import com.squareup.javapoet.ClassName;

/**
 * Created by Lachlan on 12/03/2016.
 */
public class HandleResult {
    public final ClassName originalClassName;
    public final ClassName generatedClassName;

    public HandleResult(ClassName originalClassName, ClassName generatedClassName) {
        this.originalClassName = originalClassName;
        this.generatedClassName = generatedClassName;
    }
}
