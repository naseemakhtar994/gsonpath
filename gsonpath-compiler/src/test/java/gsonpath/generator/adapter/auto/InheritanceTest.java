package gsonpath.generator.adapter.auto;

import gsonpath.generator.BaseGeneratorTest;
import org.junit.Test;

public class InheritanceTest extends BaseGeneratorTest {
    @Test
    public void testInheritance() {
        assertGeneratedContent(new TestCriteria("adapter/auto/inheritance")
                .addRelativeSource("TestInheritanceBase.java")
                .addRelativeSource("TestInheritance.java")
                .addRelativeGenerated("TestInheritance_GsonTypeAdapter.java"));
    }
}
