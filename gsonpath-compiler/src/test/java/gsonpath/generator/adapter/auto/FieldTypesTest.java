package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class FieldTypesTest extends BaseGeneratorTest {
    @Test
    public void testPrimitives() {
        assertGeneratedContent(new TestCriteria("adapter/auto/field_types/primitives")
                .addRelativeSource("TestPrimitives.java")
                .addRelativeGenerated("TestPrimitives_GsonTypeAdapter.java"));
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
