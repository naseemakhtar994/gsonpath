package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class FieldAnnotationsTest extends BaseGeneratorTest {

    @Test
    public void testFlattenJson() {
        assertGeneratedContent(new TestCriteria("adapter/auto/field_annotations/flatten_json")
                .addRelativeSource("TestFlattenJson.java")
                .addRelativeGenerated("TestFlattenJson_GsonTypeAdapter.java"));
    }

    @Test
    public void testExcludeFields() {
        assertGeneratedContent(new TestCriteria("adapter/auto/field_annotations/exclude")
                .addRelativeSource("TestExclude.java")
                .addRelativeGenerated("TestExclude_GsonTypeAdapter.java"));
    }
}
