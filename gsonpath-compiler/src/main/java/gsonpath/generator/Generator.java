package gsonpath.generator;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

public abstract class Generator {
    public final ProcessingEnvironment processingEnv;

    public Generator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public boolean writeFile(String packageName, TypeSpec.Builder typeBuilder) {
        try {
            JavaFile.Builder builder = JavaFile.builder(packageName, typeBuilder.build());
            onJavaFileBuilt(builder);
            builder.build().writeTo(processingEnv.getFiler());
            return true;

        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error while writing javapoet file: " + e.getMessage());

            return false;
        }
    }

    public void onJavaFileBuilt(JavaFile.Builder builder) {
        // Do nothing.
    }
}
