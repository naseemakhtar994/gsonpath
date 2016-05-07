package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class DelimiterTest extends BaseGeneratorTest {

    @Test
    public void testStandardDelimiter() {
        assertGeneratedContent(new TestCriteria("adapter/auto/delimiter/standard")
                .addRelativeSource("TestStandardDelimiter.java")
                .addRelativeGenerated("TestStandardDelimiter_GsonTypeAdapter.java"));
    }

    @Test
    public void testCustomDelimiter() {
        assertGeneratedContent(new TestCriteria("adapter/auto/delimiter/custom")
                .addRelativeSource("TestCustomDelimiter.java")
                .addRelativeGenerated("TestCustomDelimiter_GsonTypeAdapter.java"));
    }

    @Test
    public void testMultipleDelimiters() {
        assertGeneratedContent(new TestCriteria("adapter/auto/delimiter/multiple")
                .addRelativeSource("TestMultipleDelimiters.java")
                .addRelativeGenerated("TestMultipleDelimiters_GsonTypeAdapter.java"));
    }

}
