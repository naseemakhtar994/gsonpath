package gsonpath.generator.adapter.auto;

import com.google.testing.compile.JavaFileObjects;
import gsonpath.GsonProcessor;
import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class SubstitutionTest extends BaseGeneratorTest {
    @Test
    public void testValidSubstitution() {
        assertGeneratedContent(new TestCriteria("adapter/auto/substitution/valid")
                .addRelativeSource("TestSubstitutionBase.java")
                .addRelativeSource("TestSubstitutionImpl1.java")
                .addRelativeSource("TestSubstitutionImpl2.java")
                .addRelativeGenerated("TestSubstitutionImpl1_GsonTypeAdapter.java")
                .addRelativeGenerated("TestSubstitutionImpl2_GsonTypeAdapter.java"));
    }

    @Test
    public void testSubstitutionError_duplicateKeys() {
        JavaFileObject source = JavaFileObjects.forResource("adapter/auto/substitution/errors/TestSubstitutionError_DuplicateKeys.java");

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .failsToCompile()
                .withErrorContaining("PathSubstitution original values must be unique")
                .in(source);
    }


}
