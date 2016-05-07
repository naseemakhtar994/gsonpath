package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class NestedClassTest extends BaseGeneratorTest {
    @Test
    public void testNestedClasses() {
        assertGeneratedContent(new TestCriteria("adapter/auto/nested_class")
                .addRelativeSource("TestNestedClass.java")
                .addRelativeGenerated("TestNestedClass_Nested_GsonTypeAdapter.java"));
    }
}
