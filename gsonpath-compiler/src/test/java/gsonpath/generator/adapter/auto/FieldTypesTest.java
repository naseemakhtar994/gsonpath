package gsonpath.generator.adapter.auto;

import com.google.testing.compile.JavaFileObjects;
import gsonpath.GsonProcessor;
import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class FieldTypesTest extends BaseGeneratorTest {
    @Test
    public void testValidPrimitives() {
        assertGeneratedContent(new TestCriteria("adapter/auto/field_types/primitives/valid")
                .addRelativeSource("TestValidPrimitives.java")
                .addRelativeGenerated("TestValidPrimitives_GsonTypeAdapter.java"));
    }

    @Test
    public void testInvalidPrimitives() {
        JavaFileObject source = JavaFileObjects.forResource("adapter/auto/field_types/primitives/invalid/TestInvalidPrimitives.java");

        assertAbout(javaSource()).that(source)
                .processedWith(new GsonProcessor())
                .failsToCompile()
                .withErrorContaining("Gson Path: Unsupported primitive type found. Only boolean, int, double and long can be used.")
                .in(source)
                .onLine(7);
    }

    @Test
    public void testBoxedPrimitives() {
        assertGeneratedContent(new TestCriteria("adapter/auto/field_types/boxed_primitives")
                .addRelativeSource("TestBoxedPrimitives.java")
                .addRelativeGenerated("TestBoxedPrimitives_GsonTypeAdapter.java"));
    }

    @Test
    public void testGenericFields() {
        assertGeneratedContent(new TestCriteria("adapter/auto/field_types/generics")
                .addRelativeSource("TestGenerics.java")
                .addRelativeGenerated("TestGenerics_GsonTypeAdapter.java"));
    }

    @Test
    public void testCustomFieldType() {
        assertGeneratedContent(new TestCriteria("adapter/auto/field_types/custom_field")
                .addRelativeSource("TestCustomField.java")
                .addRelativeGenerated("TestCustomField_GsonTypeAdapter.java"));
    }

}
