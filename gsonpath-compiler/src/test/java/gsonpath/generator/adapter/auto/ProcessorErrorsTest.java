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
    public void testInvalidType() {
        JavaFileObject source = JavaFileObjects.forResource("adapter/auto/processor_errors/TestProcessorErrors.java");

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .failsToCompile()
                .withErrorContaining("Invalid field type: java.lang.Object")
                .in(source)
                .onLine(8);
    }
}
