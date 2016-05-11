package gsonpath.generator.adapter.auto;

import com.google.testing.compile.JavaFileObjects;
import gsonpath.GsonProcessor;
import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ProcessorErrorsTest extends BaseGeneratorTest {
    @Test
    public void testInvalidFieldType() {
        JavaFileObject source = JavaFileObjects.forResource("adapter/auto/processor_errors/TestInvalidFieldTypeError.java");

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .failsToCompile()
                .withErrorContaining("Invalid field type: java.lang.Object")
                .in(source)
                .onLine(8);
    }

    @Test
    public void testInvalidFieldPath() {
        JavaFileObject source = JavaFileObjects.forResource("adapter/auto/processor_errors/TestInvalidFieldPathError.java");

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .failsToCompile()
                .withErrorContaining("Unexpected duplicate field 'value' found. Each tree branch must use a unique value!")
                .in(source)
                .onLine(16);
    }

    @Test
    public void testDuplicateFieldError() {
        JavaFileObject source = JavaFileObjects.forResource("adapter/auto/processor_errors/TestDuplicateFieldError.java");

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .failsToCompile()
                .withErrorContaining("Unexpected duplicate field 'value' found. Each tree branch must use a unique value!")
                .in(source)
                .onLine(13);
    }
}
